package com.escuelafutbol.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EquipacionResponseDTO(
        Long id,
        String nombreJugador,
        BigDecimal importe,
        LocalDate fechaPago,
        String motivo,
        String temporada
) {}
