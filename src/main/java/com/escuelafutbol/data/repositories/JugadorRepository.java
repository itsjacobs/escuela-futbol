package com.escuelafutbol.data.repositories;

import com.escuelafutbol.domain.model.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JugadorRepository extends JpaRepository<Jugador, Long> {
    List<Jugador> findByTutorId(Long tutorId);
}
