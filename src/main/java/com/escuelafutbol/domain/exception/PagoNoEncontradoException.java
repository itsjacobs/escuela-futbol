package com.escuelafutbol.domain.exception;

/**
 * Excepcion de dominio para accesos a pagos que no existen.
 * <p>
 * Se lanza en consultas o actualizaciones cuando el identificador de pago
 * no corresponde con ningun registro persistido.
 * </p>
 */
public class PagoNoEncontradoException extends RuntimeException {

    /**
     * Construye la excepcion con el motivo funcional del no encontrado.
     *
     * @param message descripcion del pago no localizado
     */
    public PagoNoEncontradoException(String message) {
        super(message);
    }
}

