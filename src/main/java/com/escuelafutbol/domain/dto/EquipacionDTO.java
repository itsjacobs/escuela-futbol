package com.escuelafutbol.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record EquipacionDTO(
        @NotNull(message = "El jugador es obligatorio")
        Long jugadorId,

        @NotBlank(message = "El motivo es obligatorio")
        String motivo,  // NUEVA_INSCRIPCION, ROTURA, TALLA_PEQUEÑA, OTRO

        @NotNull(message = "El importe es obligatorio")
        BigDecimal importe
) {}