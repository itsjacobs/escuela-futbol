package com.escuelafutbol.domain.exception;

/**
 * Excepcion generica para violaciones de reglas de negocio del dominio.
 * <p>
 * Permite comunicar errores funcionales sin asociarlos a una entidad concreta.
 * </p>
 */
public class ReglaNegocioException extends RuntimeException {

    /**
     * Crea la excepcion con el detalle de la regla incumplida.
     *
     * @param message descripcion de la validacion de negocio fallida
     */
    public ReglaNegocioException(String message) {
        super(message);
    }
}

