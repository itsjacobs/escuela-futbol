package com.escuelafutbol.data.repositories;

import com.escuelafutbol.domain.model.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio JPA para entidad {@link Tutor}.
 * <p>
 * EN: Data access contract for tutor persistence operations.
 * ES: Contrato de acceso a datos para operaciones de persistencia de tutores.
 */
public interface TutorRepository extends JpaRepository<Tutor, Long> {
  /**
   * Busca un tutor por email único.
   *
   * @param email email del tutor
   * @return tutor encontrado envuelto en {@link Optional}
   */
    Optional<Tutor> findByEmail(String email);
}
