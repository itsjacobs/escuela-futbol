package com.escuelafutbol.domain.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PagoDTO(
        @NotNull(message = "El jugador es obligatorio")
        Long jugadorId,
        @NotNull(message = "El importe es obligatorio")
        BigDecimal importe,
        String metodoPago,// ONLINE o EFECTIVO
        String concepto,
        String registradoPor //solo si es EFECTIVO
) {
}
