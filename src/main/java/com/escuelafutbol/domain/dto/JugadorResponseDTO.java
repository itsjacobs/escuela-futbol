package com.escuelafutbol.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de salida con el detalle de un jugador para vistas de tutores.
 * <p>
 * Expone informacion personal, estado administrativo y resumen economico
 * del jugador en la temporada activa.
 * </p>
 *
 * @param id identificador del jugador
 * @param nombre nombre del jugador
 * @param apellidos apellidos del jugador
 * @param fechaNacimiento fecha de nacimiento
 * @param categoria categoria deportiva asignada
 * @param fechaInscripcion fecha de alta en la escuela
 * @param temporadaActual codigo o etiqueta de la temporada vigente
 * @param cuotaTemporada importe total de la cuota anual o de temporada
 * @param totalPagado suma acumulada de pagos confirmados
 * @param pendiente importe pendiente de abono
 * @param necesitaEquipacion indica si el jugador requiere equipacion
 * @param equipacionConfirmada indica si la equipacion ya fue regularizada
 * @param tieneCuotaPendiente indica si mantiene deuda activa
 * @param numeroCuotas numero de cuotas pactadas
 */
public record JugadorResponseDTO(
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
        boolean necesitaEquipacion,
        boolean equipacionConfirmada,
        boolean tieneCuotaPendiente,
        Integer numeroCuotas
) {}
