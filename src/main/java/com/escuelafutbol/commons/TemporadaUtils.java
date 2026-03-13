package com.escuelafutbol.commons;

import java.time.LocalDate;

/**
 * Utilidad estática para operaciones de temporada deportiva.
 * <p>
 * EN: Utility class for season-related calculations.
 * ES: Clase utilitaria para cálculos relacionados con temporada.
 */
public final class TemporadaUtils {

  /**
   * Constructor privado para evitar instanciación.
   */
    private TemporadaUtils() {
    }

  /**
   * Calcula la temporada activa en formato {@code AAAA-AAAA}.
   * <p>
   * Regla: a partir de julio comienza la siguiente temporada.
   *
   * @return temporada actual según fecha del sistema
   */
    public static String calcularTemporadaActual() {
        int anio = LocalDate.now().getYear();
        if (LocalDate.now().getMonthValue() >= 7) {
            return anio + Constantes.GUION + (anio + 1);
        }
        return (anio - 1) + Constantes.GUION + anio;
    }
}

