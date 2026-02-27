package com.escuelafutbol.domain.service;

import com.escuelafutbol.data.repositories.JugadorRepository;
import com.escuelafutbol.data.repositories.PagoRepository;
import com.escuelafutbol.domain.dto.PagoDTO;
import com.escuelafutbol.domain.dto.PagoResponseDTO;
import com.escuelafutbol.domain.model.Jugador;
import com.escuelafutbol.domain.model.MetodoPago;
import com.escuelafutbol.domain.model.Pago;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
                pago.getJugador().getNombre(),
                pago.getImporte(),
                pago.getFechaPago(),
                pago.getMetodoPago().name(),
                pago.getConcepto()
        );
    }

    public PagoResponseDTO registrarPago(PagoDTO pagoDTO) {
        if (pagoDTO.jugadorId() == null) {
            throw new RuntimeException("jugadorId es null");
        }

        Jugador jugador = jugadorRepository.findById(pagoDTO.jugadorId())
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado con id: " + pagoDTO.jugadorId()));

        Pago pago = new Pago();
        pago.setJugador(jugador);
        pago.setImporte(pagoDTO.importe());
        pago.setFechaPago(LocalDate.now());
        pago.setMetodoPago(MetodoPago.valueOf(pagoDTO.metodoPago()));
        pago.setConcepto(pagoDTO.concepto());
        pago.setRegistradoPor(pagoDTO.registradoPor());

        Pago savedPago = repository.save(pago);
        return convertirAResponseDTO(savedPago);
    }

    public List<PagoResponseDTO> findByJugadorId(Long jugadorId){
        return repository.findByJugadorId(jugadorId)
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }
    public List<PagoResponseDTO> findAll(){
        return repository.findAll()
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }
    public BigDecimal getTotalPagado(Long jugadorId){
        return repository.findByJugadorId(jugadorId)
                .stream()
                .map(Pago::getImporte)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public BigDecimal getPendiente(Long jugadorId){
        Jugador jugador = jugadorRepository.findById(jugadorId)
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));
        return jugador.getCuotaTemporada().subtract(getTotalPagado(jugadorId));
    }
    public void delete(Long id){
        repository.deleteById(id);
    }
}
