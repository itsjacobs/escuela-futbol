package com.escuelafutbol.ui.api;

import com.escuelafutbol.data.repositories.TutorRepository;
import com.escuelafutbol.domain.dto.JugadorAdminResponseDTO;
import com.escuelafutbol.domain.dto.JugadorDTO;
import com.escuelafutbol.domain.dto.JugadorResponseDTO;
import com.escuelafutbol.domain.model.Tutor;
import com.escuelafutbol.domain.service.JugadorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jugadores")
public class JugadorApiController {

    private final JugadorService jugadorService;
    private final TutorRepository tutorRepository;

    public JugadorApiController(JugadorService jugadorService, TutorRepository tutorRepository) {
        this.jugadorService = jugadorService;
        this.tutorRepository = tutorRepository;
    }

    @PostMapping
    public ResponseEntity<JugadorResponseDTO> inscribirJugador(
            @Valid @RequestBody JugadorDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        Tutor tutor = tutorRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Tutor no encontrado"));

        JugadorDTO dtoConTutor = new JugadorDTO(
                dto.nombre(),
                dto.apellidos(),
                dto.fechaNacimiento(),
                dto.necesitaEquipacion(),
                tutor.getId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(jugadorService.save(dtoConTutor));
    }

    @GetMapping
    public ResponseEntity<List<JugadorResponseDTO>> findAll() {
        return ResponseEntity.ok(jugadorService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JugadorResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(jugadorService.findById(id));
    }

    @GetMapping("/tutor/{id}")
    public ResponseEntity<List<JugadorResponseDTO>> findByTutorId(@PathVariable Long id) {
        return ResponseEntity.ok(jugadorService.findByTutorId(id));
    }

    @GetMapping("/tutor/me")
    public ResponseEntity<List<JugadorResponseDTO>> misJugadores(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(jugadorService.findByEmail(userDetails.getUsername()));
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<JugadorResponseDTO>> findByCategoria(@PathVariable String categoria) {
        return ResponseEntity.ok(jugadorService.findByCategoria(categoria));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        jugadorService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/admin/all")
    public ResponseEntity<List<JugadorAdminResponseDTO>> findAllAdmin() {
        return ResponseEntity.ok(jugadorService.findAllAdmin());
    }

    @GetMapping("/admin/categoria/{categoria}")
    public ResponseEntity<List<JugadorAdminResponseDTO>> findByCategoriaAdmin(@PathVariable String categoria) {
        return ResponseEntity.ok(jugadorService.findByCategoriaAdmin(categoria));
    }
}