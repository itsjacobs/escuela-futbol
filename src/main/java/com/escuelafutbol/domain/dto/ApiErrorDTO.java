package com.escuelafutbol.domain.dto;

/**
 * DTO estandar de error para respuestas HTTP de la API.
 * <p>
 * Encapsula un codigo estable y un mensaje legible para cliente.
 * </p>
 *
 * @param code codigo funcional o tecnico del error
 * @param message descripcion del problema
 */
public record ApiErrorDTO(
        String code,
        String message
) {
}

