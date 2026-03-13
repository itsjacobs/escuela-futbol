package com.escuelafutbol.ui.api;

import com.escuelafutbol.commons.Constantes;
import com.escuelafutbol.domain.dto.ElegirCuotasDTO;
import com.escuelafutbol.domain.dto.PagoDTO;
import com.escuelafutbol.domain.dto.PagoResponseDTO;
import com.escuelafutbol.domain.service.PagoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para operaciones de pagos.
 * <p>
 * Gestiona pagos de transferencia y efectivo, confirmación/rechazo,
 * y flujos de cuotas (primera y siguientes) delegando en {@link PagoService}.
 * <p>
 * EN: REST API that exposes payment, confirmation, and installment endpoints.
 * ES: API REST que expone endpoints de pagos, confirmación y cuotas.
 */
@RestController
@RequestMapping(Constantes.RUTA_API_PAGOS)
public class PagoApiController {
    private final PagoService pagoService;

    /**
     * Construye el controlador de pagos.
     *
     * @param pagoService servicio de negocio de pagos
     */
    public PagoApiController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    /**
     * Registra un pago para un jugador autorizado.
     *
     * @param dto datos de pago de entrada
     * @param userDetails usuario autenticado
     * @return pago registrado
     */
    @PostMapping
    public PagoResponseDTO pagar(@Valid @RequestBody PagoDTO dto, @AuthenticationPrincipal UserDetails userDetails) {
        PagoDTO dtoConRegistrador = new PagoDTO(
                dto.jugadorId(),
                dto.importe(),
                dto.metodoPago(),
                dto.concepto(),
                userDetails.getUsername()
        );
        return pagoService.registrarPago(dtoConRegistrador, userDetails.getUsername(), isAdmin(userDetails));
    }

    /**
     * Recupera todos los pagos (uso administrativo).
     *
     * @return listado completo de pagos
     */
    @GetMapping
    public List<PagoResponseDTO> findAll(){
        return pagoService.findAll();
    }

    /**
     * Recupera pagos de un jugador concreto con control de autorización.
     *
     * @param id identificador del jugador
     * @param userDetails usuario autenticado
     * @return pagos del jugador
     */
    @GetMapping(Constantes.RUTA_PAGOS_JUGADOR_ID)
    public List<PagoResponseDTO> findByJugadorId(@PathVariable Long id,
                                                 @AuthenticationPrincipal UserDetails userDetails){
        return pagoService.findByJugadorId(id, userDetails.getUsername(), isAdmin(userDetails));
    }

    /**
     * Obtiene la deuda pendiente de cuota de un jugador autorizado.
     *
     * @param id identificador del jugador
     * @param userDetails usuario autenticado
     * @return importe pendiente
     */
    @GetMapping(Constantes.RUTA_PAGOS_PENDIENTE_ID)
    public BigDecimal deudaPendiente(@PathVariable Long id,
                                     @AuthenticationPrincipal UserDetails userDetails){
        return pagoService.getPendienteJugador(id, userDetails.getUsername(), isAdmin(userDetails));
    }

    /**
     * Obtiene el total pagado confirmado de un jugador autorizado.
     *
     * @param id identificador del jugador
     * @param userDetails usuario autenticado
     * @return total pagado confirmado
     */
    @GetMapping(Constantes.RUTA_PAGOS_TOTAL_ID)
    public BigDecimal totalPagado(@PathVariable Long id,
                                  @AuthenticationPrincipal UserDetails userDetails){
        return pagoService.getTotalPagado(id, userDetails.getUsername(), isAdmin(userDetails));
    }

    /**
     * Elimina un pago por id.
     *
     * @param id identificador del pago
     */
    @DeleteMapping(Constantes.RUTA_ID)
    public void delete(@PathVariable Long id){
        pagoService.delete(id);
    }

    /**
     * Registra un pago en efectivo.
     * <p>
     * Endpoint restringido a usuarios ADMIN por anotación de seguridad.
     *
     * @param dto datos del pago en efectivo
     * @param userDetails usuario autenticado
     * @return {@code 201 CREATED} con pago registrado
     */
    @PostMapping(Constantes.RUTA_PAGOS_EFECTIVO)
    @PreAuthorize(Constantes.PREAUTHORIZE_ADMIN)
    public ResponseEntity<PagoResponseDTO> registrarPagoEfectivo(
            @Valid @RequestBody PagoDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pagoService.registrarPagoEfectivo(dto.jugadorId(), dto.importe(), userDetails.getUsername()));
    }

    /**
     * Confirma un pago pendiente por id.
     *
     * @param id identificador del pago
     * @return pago confirmado
     */
    @PutMapping(Constantes.RUTA_PAGOS_CONFIRMAR)
    public ResponseEntity<PagoResponseDTO> confirmar(@PathVariable Long id) {
        return ResponseEntity.ok(pagoService.confirmarPago(id));
    }

    /**
     * Rechaza un pago pendiente por id.
     *
     * @param id identificador del pago
     * @return respuesta {@code 200 OK} sin cuerpo
     */
    @PutMapping(Constantes.RUTA_PAGOS_RECHAZAR)
    public ResponseEntity<Void> rechazar(@PathVariable Long id) {
        pagoService.rechazarPago(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Obtiene todos los pagos pendientes de confirmación.
     *
     * @return listado de pagos pendientes
     */
    @GetMapping(Constantes.RUTA_PAGOS_PENDIENTES)
    public List<PagoResponseDTO> getPendientes() {
        return pagoService.getPendientes();
    }

    /**
     * Define el fraccionamiento inicial de cuotas para un jugador.
     *
     * @param dto datos con jugador y número de cuotas
     * @param userDetails usuario autenticado
     * @return primer pago de cuota generado
     */
    @PostMapping(Constantes.RUTA_PAGOS_ELEGIR_CUOTAS)
    public ResponseEntity<PagoResponseDTO> elegirCuotas(
            @Valid @RequestBody ElegirCuotasDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(pagoService.generarPrimeraCuota(
                dto.jugadorId(),
                dto.numeroCuotas(),
                userDetails.getUsername(),
                isAdmin(userDetails)
        ));
    }

    /**
     * Genera la siguiente cuota pendiente para un jugador.
     *
     * @param body cuerpo con la clave {@code jugadorId}
     * @param userDetails usuario autenticado
     * @return siguiente cuota generada
     */
    @PostMapping(Constantes.RUTA_PAGOS_SIGUIENTE_CUOTA)
    public ResponseEntity<PagoResponseDTO> siguienteCuota(
            @RequestBody Map<String, Long> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(pagoService.generarSiguienteCuota(
                body.get(Constantes.CAMPO_JUGADOR_ID),
                userDetails.getUsername(),
                isAdmin(userDetails)
        ));
    }

    /**
     * Determina si el usuario autenticado tiene autoridad ADMIN.
     *
     * @param userDetails usuario autenticado
     * @return {@code true} si tiene rol/authority ADMIN
     */
    private boolean isAdmin(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> Constantes.AUTHORITY_ADMIN.equals(a.getAuthority()));
    }

}
