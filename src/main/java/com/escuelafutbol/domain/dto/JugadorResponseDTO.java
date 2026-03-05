package com.escuelafutbol.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

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