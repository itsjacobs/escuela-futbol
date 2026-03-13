package com.escuelafutbol.data.repositories;

import com.escuelafutbol.domain.model.Equipacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio JPA para entidad {@link Equipacion}.
 * <p>
 * EN: Data access contract for kit records per player and season.
 * ES: Contrato de acceso a datos para registros de equipación por jugador y temporada.
 */
public interface EquipacionRepository extends JpaRepository<Equipacion, Long> {
  /**
   * Obtiene equipaciones por jugador.
   *
   * @param jugadorId identificador del jugador
   * @return equipaciones del jugador
   */
    List<Equipacion> findByJugadorId(Long jugadorId);

  /**
   * Indica si existe equipación para jugador y temporada.
   *
   * @param jugadorId identificador del jugador
   * @param temporada temporada objetivo
   * @return {@code true} si existe registro; {@code false} en caso contrario
   */
    boolean existsByJugadorIdAndTemporada(Long jugadorId,String temporada);
}
