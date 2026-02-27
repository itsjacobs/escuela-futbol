package com.escuelafutbol.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record JugadorDTO(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,
        @NotBlank(message = "Los apellidos son obligatorios")
        String apellidos,
        @NotNull(message = "La fecha de nacimiento es obligatoria")
        LocalDate fechaNacimiento,
        boolean necesitaEquipacion,
        Long tutorId
) {
}
