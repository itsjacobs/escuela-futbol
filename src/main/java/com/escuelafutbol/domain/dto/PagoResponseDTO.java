package com.escuelafutbol.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PagoResponseDTO(
        Long id,
        String nombreJugador,
        BigDecimal importe,
        LocalDate fechaPago,
        String metodoPago,
        String concepto,
        String estado,
        String registradoPor
) {
}
