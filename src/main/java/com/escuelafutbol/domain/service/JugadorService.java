package com.escuelafutbol.domain.service;

import com.escuelafutbol.data.repositories.EquipacionRepository;
import com.escuelafutbol.data.repositories.JugadorRepository;
import com.escuelafutbol.data.repositories.PagoRepository;
import com.escuelafutbol.data.repositories.TutorRepository;
import com.escuelafutbol.domain.dto.JugadorAdminResponseDTO;
import com.escuelafutbol.domain.dto.JugadorDTO;
import com.escuelafutbol.domain.dto.JugadorResponseDTO;
import com.escuelafutbol.domain.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class JugadorService {

    private final JugadorRepository jugadorRepository;
    private final TutorRepository tutorRepository;
    private final PagoRepository pagoRepository;
    private final EquipacionRepository equipacionRepository;

    public JugadorService(JugadorRepository jugadorRepository, TutorRepository tutorRepository,
                          PagoRepository pagoRepository, EquipacionRepository equipacionRepository) {
        this.jugadorRepository = jugadorRepository;
        this.tutorRepository = tutorRepository;
        this.pagoRepository = pagoRepository;
        this.equipacionRepository = equipacionRepository;
    }

    @Transactional
    public JugadorResponseDTO save(JugadorDTO dto, String emailTutor) {
        Tutor tutor = tutorRepository.findByEmail(emailTutor)
                .orElseThrow(() -> new RuntimeException("Tutor no encontrado"));

        String categoria = calcularCategoria(dto.fechaNacimiento());
        BigDecimal cuota = calcularCuota(categoria);
        String temporada = calcularTemporadaActual();

        Jugador jugador = new Jugador();
        jugador.setNombre(dto.nombre());
        jugador.setApellidos(dto.apellidos());
        jugador.setFechaNacimiento(dto.fechaNacimiento());
        jugador.setCategoria(categoria);
        jugador.setNecesitaEquipacion(dto.necesitaEquipacion());
        jugador.setTutor(tutor);
        jugador.setEstado(EstadoJugador.PENDIENTE);
        jugador.setNumeroCuotas(dto.numeroCuotas() != null ? dto.numeroCuotas() : 1);
        jugador.setFechaInscripcion(LocalDate.now());
        jugador.setCuotaTemporada(cuota);
        jugador.setTemporadaActual(temporada);
        jugador = jugadorRepository.save(jugador);

        // Crear pago inicial pendiente
        Pago pago = new Pago();
        pago.setJugador(jugador);
        pago.setEstado(EstadoPago.PENDIENTE);
        pago.setMetodoPago(MetodoPago.TRANSFERENCIA);
        pago.setFechaPago(LocalDate.now());
        pago.setRegistradoPor(emailTutor);

        BigDecimal importe;
        String concepto;

        if (dto.necesitaEquipacion()) {
            importe = BigDecimal.valueOf(160);
            concepto = "EQUIP-" + jugador.getApellidos().split(" ")[0].toUpperCase()
                    + "-" + categoria.toUpperCase();
        } else {
            int cuotas = dto.numeroCuotas() != null ? dto.numeroCuotas() : 1;
            importe = cuota.divide(BigDecimal.valueOf(cuotas), 2, RoundingMode.HALF_UP);
            concepto = "CUOTA1-" + jugador.getApellidos().split(" ")[0].toUpperCase()
                    + "-" + categoria.toUpperCase();
        }

        pago.setImporte(importe);
        pago.setConcepto(concepto);
        pagoRepository.save(pago);

        return convertirAResponseDTO(jugador);
    }

    @Transactional
    public void delete(Long id) {
        Jugador jugador = jugadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));
        pagoRepository.deleteAll(jugador.getPagos());
        equipacionRepository.deleteAll(jugador.getEquipaciones());
        jugadorRepository.delete(jugador);
    }

    @Transactional
    public JugadorResponseDTO findById(Long id) {
        Jugador jugador = jugadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));
        return convertirAResponseDTO(jugador);
    }

    @Transactional
    public List<JugadorResponseDTO> findAll() {
        return jugadorRepository.findAll()
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    @Transactional
    public List<JugadorResponseDTO> findByCategoria(String categoria) {
        return jugadorRepository.findAll()
                .stream()
                .filter(j -> j.getCategoria().equals(categoria))
                .map(this::convertirAResponseDTO)
                .toList();
    }

    @Transactional
    public List<JugadorResponseDTO> findByTutorId(Long tutorId) {
        return jugadorRepository.findByTutorId(tutorId)
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    @Transactional
    public List<JugadorResponseDTO> findByEmail(String email) {
        return jugadorRepository.findAll()
                .stream()
                .filter(j -> j.getTutor().getEmail().equals(email))
                .map(this::convertirAResponseDTO)
                .toList();
    }

    @Transactional
    public List<JugadorResponseDTO> findByTutor(String emailTutor) {
        return jugadorRepository.findByTutorEmailAndEstadoConPagos(emailTutor, EstadoJugador.ACTIVO)
                .stream().map(this::convertirAResponseDTO).toList();
    }

    @Transactional
    public List<JugadorAdminResponseDTO> findAllAdmin() {
        return jugadorRepository.findAllConPagos()
                .stream().map(this::convertirAAdminResponseDTO).toList();
    }

    @Transactional
    public List<JugadorAdminResponseDTO> findByCategoriaAdmin(String categoria) {
        return jugadorRepository.findByCategoriaConPagos(categoria)
                .stream().map(this::convertirAAdminResponseDTO).toList();
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

    private JugadorResponseDTO convertirAResponseDTO(Jugador jugador) {
        List<Pago> pagos = jugador.getPagos() == null ? List.of() : jugador.getPagos();

        // Solo sumar pagos de CUOTA confirmados, no equipación
        BigDecimal totalCuotaPagada = pagos.stream()
                .filter(p -> p.getEstado() == EstadoPago.CONFIRMADO)
                .filter(p -> p.getConcepto() != null && p.getConcepto().startsWith("CUOTA"))
                .map(Pago::getImporte)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal cuota = jugador.getCuotaTemporada() != null ? jugador.getCuotaTemporada() : BigDecimal.ZERO;
        BigDecimal pendiente = cuota.subtract(totalCuotaPagada);

        // Total pagado real (cuota + equipación)
        BigDecimal totalPagado = pagos.stream()
                .filter(p -> p.getEstado() == EstadoPago.CONFIRMADO)
                .map(Pago::getImporte)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        boolean equipacionConfirmada = pagos.stream()
                .anyMatch(p -> p.getEstado() == EstadoPago.CONFIRMADO
                        && p.getConcepto() != null
                        && p.getConcepto().startsWith("EQUIP"));

        boolean tieneCuotaPendiente = pagos.stream()
                .anyMatch(p -> p.getEstado() == EstadoPago.PENDIENTE
                        && p.getConcepto() != null
                        && p.getConcepto().startsWith("CUOTA"));

        return new JugadorResponseDTO(
                jugador.getId(), jugador.getNombre(), jugador.getApellidos(),
                jugador.getFechaNacimiento(), jugador.getCategoria(),
                jugador.getFechaInscripcion(), jugador.getTemporadaActual(),
                cuota, totalPagado, pendiente,
                jugador.isNecesitaEquipacion(), equipacionConfirmada,
                tieneCuotaPendiente, jugador.getNumeroCuotas()
        );
    }

    private JugadorAdminResponseDTO convertirAAdminResponseDTO(Jugador jugador) {
        List<Pago> pagos = jugador.getPagos() == null ? List.of() : jugador.getPagos();

        // Pendiente = solo cuotas, no equipación
        BigDecimal totalCuotaPagada = pagos.stream()
                .filter(p -> p.getEstado() == EstadoPago.CONFIRMADO)
                .filter(p -> p.getConcepto() != null && p.getConcepto().startsWith("CUOTA"))
                .map(Pago::getImporte)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal cuota = jugador.getCuotaTemporada() != null ? jugador.getCuotaTemporada() : BigDecimal.ZERO;
        BigDecimal pendiente = cuota.subtract(totalCuotaPagada);

        // Total pagado real incluye equipación
        BigDecimal totalPagado = pagos.stream()
                .filter(p -> p.getEstado() == EstadoPago.CONFIRMADO)
                .map(Pago::getImporte)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new JugadorAdminResponseDTO(
                jugador.getId(),
                jugador.getNombre(),
                jugador.getApellidos(),
                jugador.getFechaNacimiento(),
                jugador.getCategoria(),
                jugador.getFechaInscripcion(),
                jugador.getTemporadaActual(),
                cuota,
                totalPagado,
                pendiente,
                jugador.getTutor().getNombre(),
                jugador.getTutor().getApellidos(),
                jugador.getTutor().getEmail(),
                jugador.getTutor().getTelefono()
        );
    }
}