package com.escuelafutbol.ui.api;

import com.escuelafutbol.commons.Constantes;
import com.escuelafutbol.data.repositories.TutorRepository;
import com.escuelafutbol.domain.dto.JugadorAdminResponseDTO;
import com.escuelafutbol.domain.dto.JugadorDTO;
import com.escuelafutbol.domain.dto.JugadorResponseDTO;
import com.escuelafutbol.domain.exception.TutorNoEncontradoException;
import com.escuelafutbol.domain.model.Tutor;
import com.escuelafutbol.domain.service.JugadorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para operaciones de jugadores.
 * <p>
 * Expone endpoints para inscripción, consulta y administración,
 * delegando la lógica de negocio en {@link JugadorService}.
 * <p>
 * EN: REST API for player enrollment and query operations.
 * ES: API REST para inscripción y consulta de jugadores.
 */
@RestController
@RequestMapping(Constantes.RUTA_API_JUGADORES)
public class JugadorApiController {

    private final JugadorService jugadorService;
    private final TutorRepository tutorRepository;

    /**
     * Construye el controlador con servicios/repositorios necesarios.
     *
     * @param jugadorService servicio de negocio de jugadores
     * @param tutorRepository repositorio de tutores para resolver propietario autenticado
     */
    public JugadorApiController(JugadorService jugadorService, TutorRepository tutorRepository) {
        this.jugadorService = jugadorService;
        this.tutorRepository = tutorRepository;
    }

    /**
     * Endpoint para inscribir un nuevo jugador bajo el tutor autenticado.
     * <p>
     * El {@code tutorId} de entrada no se toma del cliente: se resuelve
     * desde el usuario autenticado para evitar suplantaciones.
     *
     * @param dto datos de inscripción del jugador
     * @param userDetails usuario autenticado
     * @return {@code 201 CREATED} con jugador registrado
     * @throws TutorNoEncontradoException si el tutor autenticado no existe en base de datos
     */
    @PostMapping
    public ResponseEntity<JugadorResponseDTO> inscribirJugador(
            @Valid @RequestBody JugadorDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        Tutor tutor = tutorRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new TutorNoEncontradoException(Constantes.MENSAJE_TUTOR_NO_ENCONTRADO));

        JugadorDTO dtoConTutor = new JugadorDTO(
                dto.nombre(),
                dto.apellidos(),
                dto.fechaNacimiento(),
                dto.categoria(),
                dto.necesitaEquipacion(),
                tutor.getId(),
                dto.numeroCuotas()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jugadorService.save(dtoConTutor, userDetails.getUsername()));
    }

    /**
     * Obtiene todos los jugadores (uso administrativo).
     *
     * @return listado completo de jugadores
     */
    @GetMapping
    public ResponseEntity<List<JugadorResponseDTO>> findAll() {
        return ResponseEntity.ok(jugadorService.findAll());
    }

    /**
     * Obtiene un jugador por id aplicando control de acceso por usuario.
     *
     * @param id identificador del jugador
     * @param userDetails usuario autenticado
     * @return jugador solicitado si está autorizado
     */
    @GetMapping("/{id}")
    public ResponseEntity<JugadorResponseDTO> findById(@PathVariable Long id,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(jugadorService.findByIdParaUsuario(id, userDetails.getUsername(), isAdmin(userDetails)));
    }

    /**
     * Lista jugadores por tutor (endpoint exclusivo de administración).
     *
     * @param id identificador del tutor
     * @return jugadores asociados al tutor indicado
     */
    @GetMapping(Constantes.RUTA_JUGADORES_TUTOR_ID)
    @PreAuthorize(Constantes.PREAUTHORIZE_ADMIN)
    public ResponseEntity<List<JugadorResponseDTO>> findByTutorId(@PathVariable Long id) {
        return ResponseEntity.ok(jugadorService.findByTutorId(id));
    }

    /**
     * Obtiene los jugadores del tutor autenticado.
     *
     * @param userDetails usuario autenticado
     * @return jugadores del propio tutor
     */
    @GetMapping(Constantes.RUTA_JUGADORES_TUTOR_ME)
    public ResponseEntity<List<JugadorResponseDTO>> misJugadores(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(jugadorService.findByTutor(userDetails.getUsername()));
    }

    /**
     * Lista jugadores por categoría.
     *
     * @param categoria categoría deportiva
     * @return jugadores de la categoría indicada
     */
    @GetMapping(Constantes.RUTA_JUGADORES_CATEGORIA)
    public ResponseEntity<List<JugadorResponseDTO>> findByCategoria(@PathVariable String categoria) {
        return ResponseEntity.ok(jugadorService.findByCategoria(categoria));
    }

    /**
     * Elimina un jugador por id.
     *
     * @param id identificador del jugador
     * @return {@code 204 NO_CONTENT} si se elimina correctamente
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        jugadorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene la vista administrativa completa de jugadores.
     *
     * @return listado de jugadores con datos ampliados para administración
     */
    @GetMapping(Constantes.RUTA_JUGADORES_ADMIN_ALL)
    public ResponseEntity<List<JugadorAdminResponseDTO>> findAllAdmin() {
        return ResponseEntity.ok(jugadorService.findAllAdmin());
    }

    /**
     * Obtiene la vista administrativa de jugadores filtrada por categoría.
     *
     * @param categoria categoría deportiva
     * @return listado administrativo filtrado
     */
    @GetMapping(Constantes.RUTA_JUGADORES_ADMIN_CATEGORIA)
    public ResponseEntity<List<JugadorAdminResponseDTO>> findByCategoriaAdmin(@PathVariable String categoria) {
        return ResponseEntity.ok(jugadorService.findByCategoriaAdmin(categoria));
    }

    /**
     * Evalúa si el usuario autenticado dispone de autoridad ADMIN.
     *
     * @param userDetails usuario autenticado
     * @return {@code true} si tiene autoridad ADMIN
     */
    private boolean isAdmin(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> Constantes.AUTHORITY_ADMIN.equals(a.getAuthority()));
    }
}
