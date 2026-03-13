package com.escuelafutbol.domain.dto;

import java.time.LocalDateTime;

/**
 * DTO de salida con informacion publica de un tutor.
 * <p>
 * Se utiliza para devolver datos de perfil y auditoria basica sin exponer
 * informacion sensible como la contrasena.
 * </p>
 *
 * @param id identificador del tutor
 * @param nombre nombre del tutor
 * @param apellidos apellidos del tutor
 * @param email direccion de correo electronico
 * @param telefono telefono de contacto
 * @param rol rol funcional asignado en el sistema
 * @param fechaRegistro instante de creacion del tutor
 */
public record  TutorResponseDTO (
    Long id,
    String nombre,
    String apellidos,
    String email,
    String telefono,
    String rol,
    LocalDateTime fechaRegistro
)
{}
