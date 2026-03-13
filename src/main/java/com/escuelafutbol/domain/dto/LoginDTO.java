package com.escuelafutbol.domain.dto;

import com.escuelafutbol.commons.Constantes;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada para solicitud de autenticacion.
 * <p>
 * Contiene las credenciales minimas requeridas para iniciar sesion.
 * </p>
 *
 * @param email correo del tutor o usuario que inicia sesion
 * @param password contrasena en texto plano recibida para validacion
 */
public record LoginDTO(
    @Email(message = Constantes.MENSAJE_EMAIL_NO_VALIDO)
    @NotBlank(message = Constantes.MENSAJE_EMAIL_OBLIGATORIO)
    String email,
    @NotBlank(message = Constantes.MENSAJE_CONTRASENA_OBLIGATORIA)
    String password
) {
}
