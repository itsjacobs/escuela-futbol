package com.escuelafutbol.data.repositories;

import com.escuelafutbol.domain.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByJugadorId(Long jugadorId);
}
