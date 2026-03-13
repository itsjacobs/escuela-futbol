package com.escuelafutbol.domain.dto;

import com.escuelafutbol.commons.Constantes;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada para el registro de un nuevo tutor.
 * <p>
 * Contiene los datos iniciales necesarios para crear una cuenta con rol
 * de tutor dentro del sistema.
 * </p>
 *
 * @param nombre nombre del tutor
 * @param apellidos apellidos del tutor
 * @param email correo unico de acceso
 * @param password contrasena inicial recibida para su cifrado
 * @param telefono telefono de contacto opcional
 */
public record TutorRegisterDTO(
   @NotBlank(message = Constantes.MENSAJE_NOMBRE_OBLIGATORIO)
   String nombre,
   @NotBlank(message = Constantes.MENSAJE_APELLIDOS_OBLIGATORIOS)
   String apellidos,
   @Email(message = Constantes.MENSAJE_EMAIL_NO_VALIDO)
   @NotBlank(message = Constantes.MENSAJE_EMAIL_OBLIGATORIO)
   String email,
   @NotBlank(message = Constantes.MENSAJE_CONTRASENA_OBLIGATORIA)
   String password,
   String telefono
) {


}
