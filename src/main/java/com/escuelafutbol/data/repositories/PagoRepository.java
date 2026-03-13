package com.escuelafutbol.data.repositories;

import com.escuelafutbol.commons.Constantes;
import com.escuelafutbol.domain.model.EstadoPago;
import com.escuelafutbol.domain.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositorio JPA para entidad {@link Pago}.
 * <p>
 * EN: Data access contract for payment persistence and query operations.
 * ES: Contrato de acceso a datos para persistencia y consultas de pagos.
 */
public interface PagoRepository extends JpaRepository<Pago, Long> {
  /**
   * Obtiene pagos asociados a un jugador.
   *
   * @param jugadorId identificador del jugador
   * @return pagos del jugador
   */
    List<Pago> findByJugadorId(Long jugadorId);

  /**
   * Obtiene pagos por estado precargando jugador asociado.
   *
   * @param estado estado del pago
   * @return pagos filtrados con jugador cargado
   */
    @Query(Constantes.JPQL_PAGO_ESTADO_CON_JUGADOR)
    List<Pago> findByEstadoConJugador(@Param(Constantes.PARAM_ESTADO) EstadoPago estado);
}
