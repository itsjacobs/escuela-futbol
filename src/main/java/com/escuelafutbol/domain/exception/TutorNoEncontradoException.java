package com.escuelafutbol.domain.exception;

/**
 * Excepcion de dominio para operaciones sobre tutores inexistentes.
 * <p>
 * Se utiliza cuando no hay coincidencia por identificador u otro criterio
 * de busqueda esperado.
 * </p>
 */
public class TutorNoEncontradoException extends RuntimeException {

    /**
     * Construye la excepcion con el mensaje de negocio correspondiente.
     *
     * @param message descripcion del recurso tutor no localizado
     */
    public TutorNoEncontradoException(String message) {
        super(message);
    }
}

