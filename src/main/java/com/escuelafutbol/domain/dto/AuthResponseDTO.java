package com.escuelafutbol.domain.dto;

/**
 * DTO de respuesta para autenticación.
 * <p>
 * Contiene la información mínima necesaria para establecer sesión en cliente.
 * <p>
 * EN: Authentication response payload returned after successful login.
 * ES: Payload de respuesta devuelto tras un inicio de sesión exitoso.
 *
 * @param token token JWT emitido por el servidor
 * @param rol rol/authority del usuario autenticado
 * @param nombre nombre público a mostrar en interfaz
 */
public record AuthResponseDTO(
        String token,
        String rol,
        String nombre

) {
}
