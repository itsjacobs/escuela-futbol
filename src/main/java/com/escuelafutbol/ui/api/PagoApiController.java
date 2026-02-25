package com.escuelafutbol.ui.api;

import com.escuelafutbol.domain.dto.PagoDTO;
import com.escuelafutbol.domain.dto.PagoResponseDTO;
import com.escuelafutbol.domain.service.PagoService;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public PagoResponseDTO pagar(PagoDTO dto) {
        return pagoService.registrarPago(dto);
    }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id){
        pagoService.delete(id);
    }


}
