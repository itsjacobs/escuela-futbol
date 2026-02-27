package com.escuelafutbol.domain.service;

import com.escuelafutbol.data.repositories.EquipacionRepository;
import com.escuelafutbol.data.repositories.JugadorRepository;
import com.escuelafutbol.domain.dto.EquipacionDTO;
import com.escuelafutbol.domain.dto.EquipacionResponseDTO;
import com.escuelafutbol.domain.model.Equipacion;
import com.escuelafutbol.domain.model.Jugador;
import com.escuelafutbol.domain.model.MotivoEquipacion;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EquipacionService {

    private final EquipacionRepository repository;
    private final JugadorRepository jugadorRepository;

    public EquipacionService(EquipacionRepository repository, JugadorRepository jugadorRepository) {
        this.repository = repository;
        this.jugadorRepository = jugadorRepository;
    }

    private EquipacionResponseDTO convertirAResponseDTO(Equipacion e) {
        return new EquipacionResponseDTO(
                e.getId(),
                e.getJugador().getNombre() + " " + e.getJugador().getApellidos(),
                e.getImporte(),
                e.getFechaPago(),
                e.getMotivo().name(),
                e.getTemporada()
        );
    }

    public EquipacionResponseDTO registrarEquipacion(EquipacionDTO dto) {
        // 1. Buscar jugador
        Jugador jugador = jugadorRepository.findById(dto.jugadorId())
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));

        // 2. Calcular temporada actual
        String temporada = calcularTemporadaActual();

        // 3. Comprobar si ya tiene equipación esta temporada
        if (repository.existsByJugadorIdAndTemporada(jugador.getId(), temporada)) {
            throw new IllegalArgumentException("El jugador ya tiene equipación en la temporada " + temporada);
        }

        // 4. Crear equipación
        Equipacion equipacion = new Equipacion();
        equipacion.setJugador(jugador);
        equipacion.setImporte(dto.importe());
        equipacion.setMotivo(MotivoEquipacion.valueOf(dto.motivo()));
        equipacion.setTemporada(temporada);
        equipacion.setFechaPago(LocalDate.now());

        repository.save(equipacion);

        return convertirAResponseDTO(equipacion);
    }

    public List<EquipacionResponseDTO> findByJugadorId(Long jugadorId) {
        return repository.findByJugadorId(jugadorId)
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    public List<EquipacionResponseDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    public boolean tieneEquipacionEnTemporada(Long jugadorId, String temporada) {
        return repository.existsByJugadorIdAndTemporada(jugadorId, temporada);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private String calcularTemporadaActual() {
        int anio = LocalDate.now().getYear();
        if (LocalDate.now().getMonthValue() >= 7) {
            return anio + "-" + (anio + 1);
        } else {
            return (anio - 1) + "-" + anio;
        }
    }
}