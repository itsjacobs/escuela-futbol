package com.escuelafutbol.domain.service;

import com.escuelafutbol.data.repositories.JugadorRepository;
import com.escuelafutbol.data.repositories.PagoRepository;
import com.escuelafutbol.domain.dto.PagoDTO;
import com.escuelafutbol.domain.dto.PagoResponseDTO;
import com.escuelafutbol.domain.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PagoService {

    private final PagoRepository repository;
    private final JugadorRepository jugadorRepository;

    public PagoService(PagoRepository repository, JugadorRepository jugadorRepository) {
        this.repository = repository;
        this.jugadorRepository = jugadorRepository;
    }

    private PagoResponseDTO convertirAResponseDTO(Pago pago) {
        return new PagoResponseDTO(
                pago.getId(),
                pago.getJugador().getNombre() + " " + pago.getJugador().getApellidos(),
                pago.getImporte(),
                pago.getFechaPago(),
                pago.getMetodoPago() != null ? pago.getMetodoPago().name() : null,
                pago.getConcepto(),
                pago.getEstado() != null ? pago.getEstado().name() : null,
                pago.getRegistradoPor()
        );
    }

    @Transactional
    public PagoResponseDTO registrarPago(PagoDTO pagoDTO) {
        if (pagoDTO.jugadorId() == null) {
            throw new RuntimeException("jugadorId es null");
        }

        Jugador jugador = jugadorRepository.findById(pagoDTO.jugadorId())
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado: " + pagoDTO.jugadorId()));

        Pago pago = new Pago();
        pago.setJugador(jugador);
        pago.setImporte(pagoDTO.importe());
        pago.setFechaPago(LocalDate.now());
        pago.setMetodoPago(MetodoPago.valueOf(pagoDTO.metodoPago()));
        pago.setConcepto(pagoDTO.concepto());
        pago.setRegistradoPor(pagoDTO.registradoPor());

        // Efectivo siempre CONFIRMADO directamente
        if (MetodoPago.EFECTIVO == MetodoPago.valueOf(pagoDTO.metodoPago())) {
            pago.setEstado(EstadoPago.CONFIRMADO);
            pago.setFechaPago(LocalDate.now());
        } else {
            pago.setEstado(EstadoPago.PENDIENTE);
        }

        return convertirAResponseDTO(repository.save(pago));
    }

    @Transactional
    public PagoResponseDTO confirmarPago(Long pagoId) {
        Pago pago = repository.findById(pagoId)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));

        pago.setEstado(EstadoPago.CONFIRMADO);
        pago.setFechaPago(LocalDate.now());
        repository.save(pago);

        Jugador jugador = pago.getJugador();

        if (jugador.getEstado() == EstadoJugador.PENDIENTE) {
            jugador.setEstado(EstadoJugador.ACTIVO);
            jugadorRepository.save(jugador);
        }


        return convertirAResponseDTO(pago);
    }
    @Transactional
    public PagoResponseDTO generarPrimeraCuota(Long jugadorId, Integer numeroCuotas, String emailTutor) {
        Jugador jugador = jugadorRepository.findById(jugadorId)
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));

        // Guardar elección de cuotas
        jugador.setNumeroCuotas(numeroCuotas);
        jugadorRepository.save(jugador);

        BigDecimal importeCuota = jugador.getCuotaTemporada()
                .divide(BigDecimal.valueOf(numeroCuotas), 2, java.math.RoundingMode.HALF_UP);

        Pago pago = new Pago();
        pago.setJugador(jugador);
        pago.setEstado(EstadoPago.PENDIENTE);
        pago.setMetodoPago(MetodoPago.TRANSFERENCIA);
        pago.setFechaPago(LocalDate.now());
        pago.setRegistradoPor(emailTutor);
        pago.setImporte(importeCuota);
        pago.setConcepto("CUOTA1-"
                + jugador.getApellidos().split(" ")[0].toUpperCase()
                + "-" + jugador.getCategoria().toUpperCase());

        return convertirAResponseDTO(repository.save(pago));
    }

    @Transactional
    public void rechazarPago(Long pagoId) {
        Pago pago = repository.findById(pagoId)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado: " + pagoId));

        Jugador jugador = pago.getJugador();
        String concepto = pago.getConcepto() != null ? pago.getConcepto() : "";

        // ¿Tiene equipación ya confirmada? → es jugador activo, no borrar
        boolean tieneEquipacionConfirmada = repository.findByJugadorId(jugador.getId())
                .stream()
                .anyMatch(p -> p.getEstado() == EstadoPago.CONFIRMADO
                        && p.getConcepto() != null
                        && p.getConcepto().startsWith("EQUIP"));

        // Borrar jugador solo si es su primer pago y no tiene equipación confirmada
        boolean esPrimerPagoSinHistorial = jugador.getEstado() == EstadoJugador.PENDIENTE
                || (!tieneEquipacionConfirmada && concepto.startsWith("CUOTA1-"))
                || concepto.startsWith("EQUIP");

        if (esPrimerPagoSinHistorial && !tieneEquipacionConfirmada) {
            repository.deleteAll(jugador.getPagos());
            jugadorRepository.delete(jugador);
        } else {
            repository.delete(pago);
        }
    }

    @Transactional
    public List<PagoResponseDTO> getPendientes() {
        return repository.findByEstadoConJugador(EstadoPago.PENDIENTE)
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    public List<PagoResponseDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    public List<PagoResponseDTO> findByJugadorId(Long jugadorId) {
        return repository.findByJugadorId(jugadorId)
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    public BigDecimal getTotalPagado(Long jugadorId) {
        return repository.findByJugadorId(jugadorId)
                .stream()
                .filter(p -> p.getEstado() == EstadoPago.CONFIRMADO)
                .map(Pago::getImporte)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getPendienteJugador(Long jugadorId) {
        Jugador jugador = jugadorRepository.findById(jugadorId)
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));

        BigDecimal cuota = jugador.getCuotaTemporada() != null ? jugador.getCuotaTemporada() : BigDecimal.ZERO;

        BigDecimal totalCuotaPagada = repository.findByJugadorId(jugadorId).stream()
                .filter(p -> p.getEstado() == EstadoPago.CONFIRMADO)
                .filter(p -> p.getConcepto() != null && p.getConcepto().startsWith("CUOTA"))
                .map(Pago::getImporte)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return cuota.subtract(totalCuotaPagada);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Transactional
    public PagoResponseDTO generarSiguienteCuota(Long jugadorId, String emailTutor) {
        Jugador jugador = jugadorRepository.findById(jugadorId)
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));

        int totalCuotas = jugador.getNumeroCuotas() != null ? jugador.getNumeroCuotas() : 1;

        long cuotasConfirmadas = repository.findByJugadorId(jugadorId).stream()
                .filter(p -> p.getEstado() == EstadoPago.CONFIRMADO)
                .filter(p -> p.getConcepto() != null && p.getConcepto().startsWith("CUOTA"))
                .count();

        if (cuotasConfirmadas >= totalCuotas) {
            throw new RuntimeException("Ya se han pagado todas las cuotas");
        }

        BigDecimal importeCuota = jugador.getCuotaTemporada()
                .divide(BigDecimal.valueOf(totalCuotas), 2, java.math.RoundingMode.HALF_UP);

        Pago pago = new Pago();
        pago.setJugador(jugador);
        pago.setEstado(EstadoPago.PENDIENTE);
        pago.setMetodoPago(MetodoPago.TRANSFERENCIA);
        pago.setFechaPago(LocalDate.now());
        pago.setRegistradoPor(emailTutor);
        pago.setImporte(importeCuota);
        pago.setConcepto("CUOTA" + (cuotasConfirmadas + 1) + "-"
                + jugador.getApellidos().split(" ")[0].toUpperCase()
                + "-" + jugador.getCategoria().toUpperCase());

        return convertirAResponseDTO(repository.save(pago));
    }
    public long contarCuotasConfirmadas(Long jugadorId) {
        return repository.findByJugadorId(jugadorId).stream()
                .filter(p -> p.getEstado() == EstadoPago.CONFIRMADO)
                .filter(p -> p.getConcepto() != null && p.getConcepto().startsWith("CUOTA"))
                .count();
    }
}