package com.escuelafutbol.data.repositories;

import com.escuelafutbol.domain.model.Equipacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipacionRepository extends JpaRepository<Equipacion, Long> {
    List<Equipacion> findByJugadorId(Long jugadorId);
    boolean existsByJugadorIdAndTemporada(Long jugadorId,String temporada);
}
