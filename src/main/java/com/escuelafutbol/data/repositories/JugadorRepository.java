package com.escuelafutbol.data.repositories;

import com.escuelafutbol.commons.Constantes;
import com.escuelafutbol.domain.model.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositorio JPA para entidad {@link Jugador}.
 * <p>
 * Incluye consultas derivadas y consultas JPQL con carga de pagos
 * para evitar problemas de lazy loading en respuestas agregadas.
 * <p>
 * EN: JPA repository for player queries, including payment-prefetch variants.
 * ES: Repositorio JPA de jugadores con variantes de consulta que precargan pagos.
 */
public interface JugadorRepository extends JpaRepository<Jugador, Long> {
  /**
   * Obtiene jugadores de un tutor por su id.
   *
   * @param tutorId identificador del tutor
   * @return jugadores asociados al tutor
   */
    List<Jugador> findByTutorId(Long tutorId);


  /**
   * Obtiene todos los jugadores de un tutor por email, precargando pagos.
   *
   * @param email email del tutor
   * @return jugadores del tutor con pagos cargados
   */
    @Query(Constantes.JPQL_JUGADOR_TUTOR_EMAIL_CON_PAGOS)
    List<Jugador> findByTutorEmailConPagos(@Param(Constantes.PARAM_EMAIL) String email);

  /**
   * Obtiene todos los jugadores con pagos precargados.
   *
   * @return jugadores con pagos cargados
   */
    @Query(Constantes.JPQL_JUGADOR_ALL_CON_PAGOS)
    List<Jugador> findAllConPagos();

  /**
   * Obtiene jugadores por categoría con pagos precargados.
   *
   * @param categoria categoría deportiva
   * @return jugadores filtrados con pagos cargados
   */
    @Query(Constantes.JPQL_JUGADOR_CATEGORIA_CON_PAGOS)
    List<Jugador> findByCategoriaConPagos(@Param(Constantes.PARAM_CATEGORIA) String categoria);

}
