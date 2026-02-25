package com.escuelafutbol.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record TutorRegisterDTO(
   @NotBlank(message = "El nombre es obligatorio")
   String nombre,
   @NotBlank(message = "Los apellidos son obligatorios")
   String apellidos,
   @Email(message = "El email no es válido")
   @NotBlank(message = "El email es obligatorio")
   String email,
   @NotBlank(message = "La contraseña es obligatoria")
   String password,
   String telefono
) {


}
