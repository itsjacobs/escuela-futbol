package com.escuelafutbol.ui.api;

import com.escuelafutbol.commons.Constantes;
import com.escuelafutbol.domain.dto.ApiErrorDTO;
import com.escuelafutbol.domain.exception.CredencialesInvalidasException;
import com.escuelafutbol.domain.exception.InscripcionInvalidaException;
import com.escuelafutbol.domain.exception.JugadorNoEncontradoException;
import com.escuelafutbol.domain.exception.PagoNoEncontradoException;
import com.escuelafutbol.domain.exception.ReglaNegocioException;
import com.escuelafutbol.domain.exception.TutorNoEncontradoException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejador global de excepciones para la capa REST.
 * <p>
 * Traduce excepciones de dominio y validación a respuestas HTTP consistentes
 * en formato {@link ApiErrorDTO}.
 * <p>
 * EN: Centralized REST exception mapper for domain and validation errors.
 * ES: Mapeador centralizado de excepciones REST para errores de dominio y validación.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja errores de inscripción inválida.
     *
     * @param ex excepción de inscripción
     * @return respuesta {@code 400 BAD_REQUEST} con código funcional
     */
    @ExceptionHandler(InscripcionInvalidaException.class)
    public ResponseEntity<ApiErrorDTO> handleInscripcionInvalida(InscripcionInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorDTO(Constantes.CODIGO_ERROR_INSCRIPCION_INVALIDA, ex.getMessage()));
    }

    /**
     * Maneja violaciones de reglas de negocio.
     *
     * @param ex excepción de regla de negocio
     * @return respuesta {@code 400 BAD_REQUEST} con detalle funcional
     */
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ApiErrorDTO> handleReglaNegocio(ReglaNegocioException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorDTO(Constantes.CODIGO_ERROR_REGLA_NEGOCIO, ex.getMessage()));
    }

    /**
     * Maneja errores de recurso no encontrado.
     *
     * @param ex excepción de recurso ausente
     * @return respuesta {@code 404 NOT_FOUND}
     */
    @ExceptionHandler({JugadorNoEncontradoException.class, TutorNoEncontradoException.class, PagoNoEncontradoException.class})
    public ResponseEntity<ApiErrorDTO> handleNoEncontrado(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorDTO(Constantes.CODIGO_ERROR_RECURSO_NO_ENCONTRADO, ex.getMessage()));
    }

    /**
     * Maneja credenciales inválidas de autenticación.
     *
     * @param ex excepción de credenciales
     * @return respuesta {@code 401 UNAUTHORIZED}
     */
    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ApiErrorDTO> handleCredenciales(CredencialesInvalidasException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiErrorDTO(Constantes.CODIGO_ERROR_CREDENCIALES_INVALIDAS, ex.getMessage()));
    }

    /**
     * Maneja intentos de acceso sin permisos suficientes.
     *
     * @param ex excepción de autorización
     * @return respuesta {@code 403 FORBIDDEN}
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorDTO> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiErrorDTO(Constantes.CODIGO_ERROR_ACCESS_DENIED, ex.getMessage()));
    }

    /**
     * Maneja errores de validación de DTOs anotados con Bean Validation.
     *
     * @param ex excepción de validación de argumentos
     * @return respuesta {@code 400 BAD_REQUEST} con mensaje del primer campo inválido
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDTO> handleValidacion(MethodArgumentNotValidException ex) {
        FieldError firstError = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String message = firstError != null ? firstError.getDefaultMessage() : Constantes.MENSAJE_SOLICITUD_NO_VALIDA;

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorDTO(Constantes.CODIGO_ERROR_VALIDACION, message));
    }
}

