package com.escuelafutbol.domain.dto;

import java.time.LocalDateTime;

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
