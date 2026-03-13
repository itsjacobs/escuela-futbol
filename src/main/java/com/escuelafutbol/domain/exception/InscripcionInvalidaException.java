package com.escuelafutbol.domain.exception;

/**
 * Excepcion de dominio para intentos de inscripcion que incumplen validaciones.
 * <p>
 * Se utiliza cuando la solicitud de alta de jugador no respeta condiciones
 * funcionales de la escuela.
 * </p>
 */
public class InscripcionInvalidaException extends RuntimeException {

    /**
     * Construye la excepcion con el motivo de invalidez detectado.
     *
     * @param message detalle funcional de la inscripcion rechazada
     */
    public InscripcionInvalidaException(String message) {
        super(message);
    }
}

