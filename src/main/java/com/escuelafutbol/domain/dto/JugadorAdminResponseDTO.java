package com.escuelafutbol.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de salida para vistas administrativas de jugadores.
 * <p>
 * Amplia la informacion del jugador con datos de tutor para tareas de gestion,
 * seguimiento de pagos y contacto.
 * </p>
 *
 * @param id identificador del jugador
 * @param nombre nombre del jugador
 * @param apellidos apellidos del jugador
 * @param fechaNacimiento fecha de nacimiento
 * @param categoria categoria deportiva
 * @param fechaInscripcion fecha de alta en la escuela
 * @param temporadaActual temporada vigente asociada
 * @param cuotaTemporada cuota total establecida
 * @param totalPagado importe acumulado pagado
 * @param pendiente saldo pendiente
 * @param tutorNombre nombre del tutor
 * @param tutorApellidos apellidos del tutor
 * @param tutorEmail correo del tutor
 * @param tutorTelefono telefono del tutor
 */
public record JugadorAdminResponseDTO(
        Long id,
        String nombre,
        String apellidos,
        LocalDate fechaNacimiento,
        String categoria,
        LocalDate fechaInscripcion,
        String temporadaActual,
        BigDecimal cuotaTemporada,
        BigDecimal totalPagado,
        BigDecimal pendiente,
        String tutorNombre,
        String tutorApellidos,
        String tutorEmail,
        String tutorTelefono
) {}
