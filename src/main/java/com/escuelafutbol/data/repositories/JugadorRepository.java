package com.escuelafutbol.data.repositories;

import com.escuelafutbol.domain.model.EstadoJugador;
import com.escuelafutbol.domain.model.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JugadorRepository extends JpaRepository<Jugador, Long> {
    List<Jugador> findByTutorId(Long tutorId);
    @Query("SELECT j FROM Jugador j LEFT JOIN FETCH j.pagos WHERE j.tutor.email = :email AND j.estado = :estado")
    List<Jugador> findByTutorEmailAndEstadoConPagos(@Param("email") String email, @Param("estado") EstadoJugador estado);
    @Query("SELECT j FROM Jugador j LEFT JOIN FETCH j.pagos")
    List<Jugador> findAllConPagos();
    @Query("SELECT j FROM Jugador j LEFT JOIN FETCH j.pagos WHERE j.categoria = :categoria")
    List<Jugador> findByCategoriaConPagos(@Param("categoria") String categoria);

}
