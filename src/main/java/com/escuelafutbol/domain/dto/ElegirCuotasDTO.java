package com.escuelafutbol.domain.dto;

import com.escuelafutbol.commons.Constantes;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO de entrada para definir el numero de cuotas de un jugador.
 * <p>
 * Se utiliza cuando el tutor o administracion selecciona una modalidad
 * fraccionada de pago para la temporada.
 * </p>
 *
 * @param jugadorId identificador del jugador afectado
 * @param numeroCuotas cantidad de cuotas elegida
 */
public record ElegirCuotasDTO(
		@NotNull(message = Constantes.MENSAJE_JUGADOR_OBLIGATORIO)
		Long jugadorId,
		@NotNull(message = Constantes.MENSAJE_NUMERO_CUOTAS_OBLIGATORIO)
		@Positive(message = Constantes.MENSAJE_NUMERO_CUOTAS_INVALIDO)
		Integer numeroCuotas
) {}
