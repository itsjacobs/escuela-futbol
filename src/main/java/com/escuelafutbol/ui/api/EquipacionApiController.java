package com.escuelafutbol.ui.api;

import com.escuelafutbol.domain.dto.EquipacionDTO;
import com.escuelafutbol.domain.dto.EquipacionResponseDTO;
import com.escuelafutbol.domain.service.EquipacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipaciones")
public class EquipacionApiController {

    private final EquipacionService equipacionService;

    public EquipacionApiController(EquipacionService equipacionService) {
        this.equipacionService = equipacionService;
    }

    @PostMapping
    public ResponseEntity<EquipacionResponseDTO> crearEquipacion(@Valid @RequestBody EquipacionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipacionService.registrarEquipacion(dto));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EquipacionResponseDTO>> findAll() {
        return ResponseEntity.ok(equipacionService.findAll());
    }

    @GetMapping("/jugador/{id}")
    public ResponseEntity<List<EquipacionResponseDTO>> findByJugadorId(@PathVariable Long id) {
        return ResponseEntity.ok(equipacionService.findByJugadorId(id));
    }

    @GetMapping("/tiene/{jugadorId}/{temporada}")
    public ResponseEntity<Boolean> tieneEquipacionEnTemporada(@PathVariable Long jugadorId, @PathVariable String temporada) {
        return ResponseEntity.ok(equipacionService.tieneEquipacionEnTemporada(jugadorId, temporada));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        equipacionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}