package com.escuelafutbol.domain.dto;

import com.escuelafutbol.commons.Constantes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO de entrada para registrar un pago de cuota o concepto asociado.
 * <p>
 * Define los datos necesarios para imputar un pago a un jugador,
 * incluyendo importe, metodo y metadatos de auditoria basicos.
 * </p>
 *
 * @param jugadorId identificador del jugador que realiza el pago
 * @param importe cantidad abonada
 * @param metodoPago metodo de pago informado por el cliente
 * @param concepto descripcion opcional del concepto liquidado
 * @param registradoPor usuario o actor que registra la operacion
 */
public record PagoDTO(
        @NotNull(message = Constantes.MENSAJE_JUGADOR_OBLIGATORIO)
        Long jugadorId,
        @NotNull(message = Constantes.MENSAJE_IMPORTE_OBLIGATORIO)
        @Positive(message = Constantes.MENSAJE_IMPORTE_MAYOR_CERO)
        BigDecimal importe,
        @NotBlank(message = Constantes.MENSAJE_METODO_PAGO_OBLIGATORIO)
        String metodoPago,
        String concepto,
        String registradoPor
) {}
