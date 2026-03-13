package com.escuelafutbol.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de salida con datos de un pago registrado.
 * <p>
 * Se utiliza en listados e historicos para mostrar la informacion relevante
 * de una transaccion de cobro.
 * </p>
 *
 * @param id identificador del pago
 * @param nombreJugador nombre completo o visible del jugador
 * @param importe importe abonado
 * @param fechaPago fecha efectiva del pago
 * @param metodoPago metodo utilizado para el abono
 * @param concepto descripcion del concepto cobrado
 * @param estado estado actual de la transaccion
 * @param registradoPor usuario que registro el pago
 */
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
