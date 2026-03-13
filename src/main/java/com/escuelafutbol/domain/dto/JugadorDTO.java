package com.escuelafutbol.domain.dto;

import com.escuelafutbol.commons.Constantes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO de entrada para crear o actualizar datos basicos de un jugador.
 * <p>
 * Transporta informacion personal y parametros administrativos iniciales,
 * como el tutor asociado y la configuracion de cuotas.
 * </p>
 *
 * @param nombre nombre del jugador
 * @param apellidos apellidos del jugador
 * @param fechaNacimiento fecha de nacimiento del jugador
 * @param categoria categoria deportiva asignable
 * @param necesitaEquipacion indica si solicita equipacion
 * @param tutorId identificador del tutor responsable
 * @param numeroCuotas cantidad de cuotas pactadas para la temporada
 */
public record JugadorDTO(
        @NotBlank(message = Constantes.MENSAJE_NOMBRE_OBLIGATORIO)
        String nombre,
        @NotBlank(message = Constantes.MENSAJE_APELLIDOS_OBLIGATORIOS)
        String apellidos,
        @NotNull(message = Constantes.MENSAJE_FECHA_NACIMIENTO_OBLIGATORIA)
        LocalDate fechaNacimiento,
        String categoria,
        boolean necesitaEquipacion,
        Long tutorId,
        Integer numeroCuotas
) {
}
