package com.escuelafutbol.domain.exception;

/**
 * Excepcion de dominio para recursos de jugador inexistentes.
 * <p>
 * Se emplea cuando no se puede resolver un jugador por id en una operacion
 * que requiere su presencia obligatoria.
 * </p>
 */
public class JugadorNoEncontradoException extends RuntimeException {

    /**
     * Crea la excepcion con el detalle del jugador no encontrado.
     *
     * @param message mensaje funcional para el consumidor de la API
     */
    public JugadorNoEncontradoException(String message) {
        super(message);
    }
}

