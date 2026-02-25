package com.escuelafutbol.domain.service;

import com.escuelafutbol.data.repositories.JugadorRepository;
import com.escuelafutbol.data.repositories.TutorRepository;
import com.escuelafutbol.domain.dto.JugadorDTO;
import com.escuelafutbol.domain.dto.JugadorResponseDTO;
import com.escuelafutbol.domain.model.Jugador;
import com.escuelafutbol.domain.model.Tutor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class JugadorService {

    private final JugadorRepository jugadorRepository;
    private final TutorRepository tutorRepository;

    public JugadorService(JugadorRepository jugadorRepository, TutorRepository tutorRepository) {
        this.jugadorRepository = jugadorRepository;
        this.tutorRepository = tutorRepository;
    }

    public JugadorResponseDTO save(JugadorDTO dto) {
        // 1. Buscar el tutor
        Tutor tutor = tutorRepository.findById(dto.tutorId())
                .orElseThrow(() -> new RuntimeException("Tutor no encontrado"));

        // 2. Convertir DTO → Modelo
        Jugador newJugador = new Jugador();
        newJugador.setNombre(dto.nombre());
        newJugador.setApellidos(dto.apellidos());
        newJugador.setFechaNacimiento(dto.fechaNacimiento());
        newJugador.setTutor(tutor);
        newJugador.setFechaInscripcion(LocalDate.now());
        newJugador.setTemporadaActual(calcularTemporadaActual());

        // 3. Calcular categoría y cuota
        String categoria = calcularCategoria(dto.fechaNacimiento());
        newJugador.setCategoria(categoria);
        newJugador.setCuotaTemporada(calcularCuota(categoria));

        // 4. Guardar en BD
        jugadorRepository.save(newJugador);

        // 5. Convertir Modelo → ResponseDTO y devolver
        return new JugadorResponseDTO(
                newJugador.getId(),
                newJugador.getNombre(),
                newJugador.getApellidos(),
                newJugador.getFechaNacimiento(),
                newJugador.getCategoria(),
                newJugador.getFechaInscripcion(),
                newJugador.getTemporadaActual(),
                newJugador.getCuotaTemporada(),
                BigDecimal.ZERO,                  // totalPagado: aún no ha pagado nada
                newJugador.getCuotaTemporada()    // pendiente: debe pagar la cuota completa
        );
    }

    public void delete(Long id) {
        Jugador jugador = jugadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));
        jugadorRepository.delete(jugador);
    }

    public JugadorResponseDTO findById(Long id) {
        Jugador jugador = jugadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));
        return convertirAResponseDTO(jugador);
    }

    public List<JugadorResponseDTO> findAll() {
        return jugadorRepository.findAll()
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    public List<JugadorResponseDTO> findByCategoria(String categoria) {
        return jugadorRepository.findAll()
                .stream()
                .filter(j -> j.getCategoria().equals(categoria))
                .map(this::convertirAResponseDTO)
                .toList();
    }

    public long count() {
        return jugadorRepository.count();
    }


    private JugadorResponseDTO convertirAResponseDTO(Jugador jugador) {
        BigDecimal totalPagado = jugador.getPagos() == null ? BigDecimal.ZERO :
                jugador.getPagos()
                        .stream()
                        .map(p -> p.getImporte())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendiente = jugador.getCuotaTemporada().subtract(totalPagado);

        return new JugadorResponseDTO(
                jugador.getId(),
                jugador.getNombre(),
                jugador.getApellidos(),
                jugador.getFechaNacimiento(),
                jugador.getCategoria(),
                jugador.getFechaInscripcion(),
                jugador.getTemporadaActual(),
                jugador.getCuotaTemporada(),
                totalPagado,
                pendiente
        );
    }

    public String calcularCategoria(LocalDate fecha) {
        return switch (fecha.getYear()) {
            case 2008, 2009, 2010 -> "Juvenil";
            case 2011, 2012 -> "Cadete";
            case 2013, 2014 -> "Infantil";
            case 2015, 2016 -> "Alevin";
            case 2017, 2018 -> "Benjamin";
            case 2019, 2020 -> "Prebenjamin";
            case 2021, 2022 -> "Debutante";
            default -> "Categoria incorrecta o inexistente";
        };
    }

    public List<JugadorResponseDTO> findByTutorId(Long tutorId) {
        return jugadorRepository.findByTutorId(tutorId)
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }
    public List<JugadorResponseDTO> findByEmail(String email) {
        return jugadorRepository.findAll()
                .stream()
                .filter(j -> j.getTutor().getEmail().equals(email))
                .map(this::convertirAResponseDTO)
                .toList();
    }

    private BigDecimal calcularCuota(String categoria) {
        return switch (categoria) {
            case "Prebenjamin", "Benjamin", "Debutante", "Alevin" -> BigDecimal.valueOf(280.00);
            default -> BigDecimal.valueOf(320.00);
        };
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