package com.escuelafutbol.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginDTO(
    @Email(message = "El email no es válido")
    @NotBlank(message = "El email es obligatorio")
    String email,
    @NotBlank(message = "La contraseña es obligatoria")
    String password
) {
}
