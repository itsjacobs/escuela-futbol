package com.escuelafutbol.domain.dto;

import java.math.BigDecimal;

public record PagoDTO(
        Long jugadorId,
        BigDecimal importe,
        String metodoPago,
        String concepto,
        String registradoPor
) {}
