package com.escuelafutbol.domain.dto;

import com.escuelafutbol.commons.Constantes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO de entrada para registrar o actualizar un pago de equipacion.
 * <p>
 * Agrupa los datos enviados desde cliente para asociar el cargo de equipacion
 * a un jugador concreto, indicando motivo e importe.
 * </p>
 *
 * @param jugadorId identificador del jugador al que se asocia la equipacion
 * @param motivo motivo funcional del cobro de equipacion
 * @param importe importe monetario del concepto de equipacion
 */
public record EquipacionDTO(
        @NotNull(message = Constantes.MENSAJE_JUGADOR_OBLIGATORIO)
        Long jugadorId,

        @NotBlank(message = Constantes.MENSAJE_MOTIVO_OBLIGATORIO)
        String motivo,

        @NotNull(message = Constantes.MENSAJE_IMPORTE_OBLIGATORIO)
        BigDecimal importe
) {}
