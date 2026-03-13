package com.escuelafutbol.domain.exception;

/**
 * Excepcion de dominio para intentos de autenticacion con credenciales no validas.
 * <p>
 * Se lanza cuando el email no existe o la contrasena no coincide con la
 * almacenada para el usuario.
 * </p>
 */
public class CredencialesInvalidasException extends RuntimeException {

    /**
     * Crea la excepcion con un mensaje funcional para capa API o logs.
     *
     * @param message detalle del motivo de rechazo de autenticacion
     */
    public CredencialesInvalidasException(String message) {
        super(message);
    }
}

