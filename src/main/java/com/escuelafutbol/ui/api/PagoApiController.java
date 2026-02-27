package com.escuelafutbol.ui.api;

import com.escuelafutbol.domain.dto.PagoDTO;
import com.escuelafutbol.domain.dto.PagoResponseDTO;
import com.escuelafutbol.domain.service.PagoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/pagos")
public class PagoApiController {
    private final PagoService pagoService;

    public PagoApiController(PagoService pagoService) {
        this.pagoService = pagoService;
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
        return pagoService.getPendiente(id);
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
    public ResponseEntity<PagoResponseDTO> registrarPagoEfectivo(@RequestBody PagoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.registrarPago(dto));
    }


}
