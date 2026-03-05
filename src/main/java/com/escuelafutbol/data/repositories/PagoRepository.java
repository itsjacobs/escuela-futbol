package com.escuelafutbol.data.repositories;

import com.escuelafutbol.domain.model.EstadoPago;
import com.escuelafutbol.domain.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByJugadorId(Long jugadorId);
    @Query("SELECT p FROM Pago p JOIN FETCH p.jugador WHERE p.estado = :estado")
    List<Pago> findByEstadoConJugador(@Param("estado") EstadoPago estado);
    @Query("SELECT COUNT(p) FROM Pago p WHERE p.jugador.id = :jugadorId " +
            "AND p.estado = 'CONFIRMADO' AND p.concepto LIKE 'CUOTA%'")
    long countCuotasConfirmadasByJugador(@Param("jugadorId") Long jugadorId);

    @Query("SELECT COUNT(p) > 0 FROM Pago p WHERE p.jugador.id = :jugadorId " +
            "AND p.estado = 'PENDIENTE' AND p.concepto LIKE 'CUOTA%'")
    boolean existsCuotaPendienteByJugador(@Param("jugadorId") Long jugadorId);
}
