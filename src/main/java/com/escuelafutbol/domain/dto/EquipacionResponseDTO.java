package com.escuelafutbol.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de salida para movimientos de equipacion.
 * <p>
 * Resume la informacion economica y temporal del pago de equipacion
 * asociado a un jugador.
 * </p>
 *
 * @param id identificador del registro de equipacion
 * @param nombreJugador nombre del jugador asociado
 * @param importe importe cobrado por equipacion
 * @param fechaPago fecha del abono
 * @param motivo motivo del cobro de equipacion
 * @param temporada temporada a la que aplica el cargo
 */
public record EquipacionResponseDTO(
        Long id,
        String nombreJugador,
        BigDecimal importe,
        LocalDate fechaPago,
        String motivo,
        String temporada
) {}
