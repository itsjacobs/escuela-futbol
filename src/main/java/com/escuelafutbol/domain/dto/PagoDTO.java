package com.escuelafutbol.domain.dto;

import java.math.BigDecimal;

public record PagoDTO(
        Long jugadorId,
        BigDecimal importe,
        String metodoPago,// ONLINE o EFECTIVO
        String concepto,
        String registradoPor //solo si es EFECTIVO
) {
}
