package com.escuelafutbol.domain.dto;

public record AuthResponseDTO(
        String token,
        String rol,
        String nombre

) {
}
