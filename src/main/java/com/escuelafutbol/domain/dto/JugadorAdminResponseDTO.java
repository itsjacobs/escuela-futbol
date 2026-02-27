package com.escuelafutbol.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

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