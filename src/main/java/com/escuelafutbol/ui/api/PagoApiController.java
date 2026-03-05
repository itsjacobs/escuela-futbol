package com.escuelafutbol.ui.api;

import com.escuelafutbol.domain.dto.ElegirCuotasDTO;
import com.escuelafutbol.domain.dto.JugadorResponseDTO;
import com.escuelafutbol.domain.dto.PagoDTO;
import com.escuelafutbol.domain.dto.PagoResponseDTO;
import com.escuelafutbol.domain.model.Jugador;
import com.escuelafutbol.domain.service.JugadorService;
import com.escuelafutbol.domain.service.PagoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
public class PagoApiController {
    private final PagoService pagoService;
    private final JugadorService jugadorService;

    public PagoApiController(PagoService pagoService, JugadorService jugadorService) {
        this.pagoService = pagoService;
        this.jugadorService = jugadorService;
    }

    @PostMapping
    public PagoResponseDTO pagar(@RequestBody PagoDTO dto, @AuthenticationPrincipal UserDetails userDetails) {
        PagoDTO dtoConRegistrador = new PagoDTO(
                dto.jugadorId(),
                dto.importe(),
                dto.metodoPago(),
                dto.concepto(),
                userDetails.getUsername()
        );
        return pagoService.registrarPago(dtoConRegistrador);
    }
    @GetMapping
    public List<PagoResponseDTO> findAll(){
        return pagoService.findAll();
    }
    @GetMapping("jugador/{id}")
    public List<PagoResponseDTO> findByJugadorId(@PathVariable Long id){
        return pagoService.findByJugadorId(id);
    }
    @GetMapping("pendiente/{id}")
    public BigDecimal deudaPendiente(@PathVariable Long id){
        return pagoService.getPendienteJugador(id);
    }
    @GetMapping("total/{id}")
    public BigDecimal totalPagado(@PathVariable Long id){
        return pagoService.getTotalPagado(id);
    }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){
        pagoService.delete(id);
    }
    @PostMapping("/efectivo")
    public ResponseEntity<PagoResponseDTO> registrarPagoEfectivo(
            @RequestBody PagoDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        JugadorResponseDTO jugador = jugadorService.findById(dto.jugadorId());
        if (jugador == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }else{
            long cuotasConfirmadas = pagoService.contarCuotasConfirmadas(dto.jugadorId());
            String concepto = "CUOTA" + (cuotasConfirmadas + 1) + "-"
                    + jugador.apellidos().split(" ")[0].toUpperCase()
                    + "-" + jugador.categoria().toUpperCase();

            PagoDTO dtoConConcepto = new PagoDTO(
                    dto.jugadorId(),
                    dto.importe(),
                    "EFECTIVO",
                    concepto,
                    userDetails.getUsername()
            );
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(pagoService.registrarPago(dtoConConcepto));
        }
    }
    @PutMapping("/{id}/confirmar")
    public ResponseEntity<PagoResponseDTO> confirmar(@PathVariable Long id) {
        return ResponseEntity.ok(pagoService.confirmarPago(id));
    }

    @PutMapping("/{id}/rechazar")
    public ResponseEntity<Void> rechazar(@PathVariable Long id) {
        pagoService.rechazarPago(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/pendientes")
    public List<PagoResponseDTO> getPendientes() {
        return pagoService.getPendientes();
    }

    @PostMapping("/elegir-cuotas")
    public ResponseEntity<PagoResponseDTO> elegirCuotas(
            @RequestBody ElegirCuotasDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(pagoService.generarPrimeraCuota(dto.jugadorId(), dto.numeroCuotas(), userDetails.getUsername()));
    }
    @PostMapping("/siguiente-cuota")
    public ResponseEntity<PagoResponseDTO> siguienteCuota(
            @RequestBody Map<String, Long> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(pagoService.generarSiguienteCuota(body.get("jugadorId"), userDetails.getUsername()));
    }

}
