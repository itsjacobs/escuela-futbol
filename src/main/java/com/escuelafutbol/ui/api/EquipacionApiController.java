package com.escuelafutbol.ui.api;

import com.escuelafutbol.commons.Constantes;
import com.escuelafutbol.domain.dto.EquipacionDTO;
import com.escuelafutbol.domain.dto.EquipacionResponseDTO;
import com.escuelafutbol.domain.service.EquipacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para operaciones de equipación.
 * <p>
 * Expone endpoints para alta, consulta y borrado de equipaciones,
 * aplicando autorización a nivel de endpoint y validación de propiedad
 * del recurso cuando corresponde.
 * <p>
 * EN: REST API for kit/equipment operations with role and ownership checks.
 * ES: API REST para operaciones de equipación con control por rol y propiedad.
 */
@RestController
@RequestMapping(Constantes.RUTA_API_EQUIPACIONES)
public class EquipacionApiController {

    private final EquipacionService equipacionService;

    /**
     * Construye el controlador con el servicio de equipación.
     *
     * @param equipacionService servicio de negocio de equipaciones
     */
    public EquipacionApiController(EquipacionService equipacionService) {
        this.equipacionService = equipacionService;
    }

    /**
     * Registra una nueva equipación para un jugador.
     * <p>
     * Endpoint restringido a administración mediante {@link PreAuthorize}.
     *
     * @param dto datos de equipación a registrar
     * @return {@code 201 CREATED} con equipación creada
     */
    @PostMapping
    @PreAuthorize(Constantes.PREAUTHORIZE_ADMIN)
    public ResponseEntity<EquipacionResponseDTO> crearEquipacion(@Valid @RequestBody EquipacionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipacionService.registrarEquipacion(dto));
    }

    /**
     * Recupera todas las equipaciones registradas.
     * <p>
     * Endpoint de uso administrativo.
     *
     * @return listado completo de equipaciones
     */
    @GetMapping
    @PreAuthorize(Constantes.PREAUTHORIZE_ADMIN)
    public ResponseEntity<List<EquipacionResponseDTO>> findAll() {
        return ResponseEntity.ok(equipacionService.findAll());
    }

    /**
     * Recupera equipaciones de un jugador autorizado.
     *
     * @param id identificador del jugador
     * @param userDetails usuario autenticado
     * @return equipaciones del jugador
     */
    @GetMapping(Constantes.RUTA_EQUIPACIONES_JUGADOR_ID)
    public ResponseEntity<List<EquipacionResponseDTO>> findByJugadorId(@PathVariable Long id,
                                                                       @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(equipacionService.findByJugadorIdParaUsuario(id, userDetails.getUsername(), isAdmin(userDetails)));
    }

    /**
     * Comprueba si el jugador tiene equipación en la temporada indicada.
     *
     * @param jugadorId identificador del jugador
     * @param temporada temporada a evaluar
     * @param userDetails usuario autenticado
     * @return {@code true} si existe equipación en esa temporada
     */
    @GetMapping(Constantes.RUTA_EQUIPACIONES_TIENE)
    public ResponseEntity<Boolean> tieneEquipacionEnTemporada(@PathVariable Long jugadorId,
                                                              @PathVariable String temporada,
                                                              @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(equipacionService.tieneEquipacionEnTemporadaParaUsuario(
                jugadorId,
                temporada,
                userDetails.getUsername(),
                isAdmin(userDetails)
        ));
    }

    /**
     * Elimina una equipación por id.
     * <p>
     * Endpoint restringido a administración.
     *
     * @param id identificador de la equipación
     * @return respuesta {@code 204 NO_CONTENT}
     */
    @DeleteMapping(Constantes.RUTA_ID)
    @PreAuthorize(Constantes.PREAUTHORIZE_ADMIN)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        equipacionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Determina si el usuario autenticado tiene autoridad ADMIN.
     *
     * @param userDetails usuario autenticado
     * @return {@code true} si dispone de autoridad ADMIN
     */
    private boolean isAdmin(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> Constantes.AUTHORITY_ADMIN.equals(a.getAuthority()));
    }
}
