package com.escuelafutbol.domain.service;

import com.escuelafutbol.commons.Constantes;
import com.escuelafutbol.data.repositories.TutorRepository;
import com.escuelafutbol.domain.dto.TutorRegisterDTO;
import com.escuelafutbol.domain.dto.TutorResponseDTO;
import com.escuelafutbol.domain.exception.CredencialesInvalidasException;
import com.escuelafutbol.domain.exception.TutorNoEncontradoException;
import com.escuelafutbol.domain.model.Rol;
import com.escuelafutbol.domain.model.Tutor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Servicio de dominio para la gestión de tutores y autenticación básica.
 * <p>
 * Esta capa implementa los casos de uso principales sobre tutores:
 * <ul>
 *   <li>Registro de nuevas cuentas</li>
 *   <li>Validación de credenciales de acceso</li>
 *   <li>Comprobación de duplicidad por email</li>
 * </ul>
 * <p>
 * EN: Handles tutor registration and credential validation business logic.
 * ES: Gestiona la lógica de registro y validación de credenciales de tutores.
 */
@Service
public class TutorService {
    private final TutorRepository tutorRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Construye el servicio con sus dependencias de persistencia y seguridad.
     *
     * @param tutorRepository repositorio de tutores
     * @param passwordEncoder codificador de contraseñas
     */
    public TutorService(TutorRepository tutorRepository, PasswordEncoder passwordEncoder) {
        this.tutorRepository = tutorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registra un nuevo tutor en el sistema.
     * <p>
     * Flujo ejecutado:
     * <ol>
     *   <li>Mapea datos del DTO a entidad {@link Tutor}</li>
     *   <li>Cifra la contraseña antes de persistir</li>
     *   <li>Asigna rol por defecto de tutor</li>
     *   <li>Genera fecha de registro</li>
     * </ol>
     *
     * @param dto datos de alta del tutor
     * @return DTO de respuesta con datos públicos del tutor registrado
     */
    public TutorResponseDTO register(TutorRegisterDTO dto) {
        Tutor tutor = new Tutor();
        tutor.setNombre(dto.nombre());
        tutor.setApellidos(dto.apellidos());
        tutor.setTelefono(dto.telefono());
        tutor.setEmail(dto.email());
        tutor.setPassword(passwordEncoder.encode(dto.password()));
        tutor.setRol(Rol.valueOf(Constantes.ROL_TUTOR));
        tutor.setFechaRegistro(LocalDateTime.now());

        tutorRepository.save(tutor);

        return new TutorResponseDTO(
                tutor.getId(),
                tutor.getNombre(),
                tutor.getApellidos(),
                tutor.getEmail(),
                tutor.getTelefono(),
                tutor.getRol().toString(),
                tutor.getFechaRegistro()
        );
    }

    /**
     * Valida credenciales de acceso para un tutor.
     * <p>
     * Verifica existencia por email y coincidencia de contraseña
     * utilizando comparación segura del codificador.
     *
     * @param email email del tutor
     * @param password contraseña en texto plano recibida en login
     * @return entidad de tutor autenticada
     * @throws TutorNoEncontradoException si no existe una cuenta con ese email
     * @throws CredencialesInvalidasException si la contraseña no coincide
     */
    public Tutor login(String email, String password) {
        Tutor tutor = tutorRepository.findByEmail(email)
                .orElseThrow(() -> new TutorNoEncontradoException(Constantes.MENSAJE_USUARIO_NO_ENCONTRADO));

        if (!passwordEncoder.matches(password, tutor.getPassword())) {
            throw new CredencialesInvalidasException(Constantes.MENSAJE_CONTRASENA_INCORRECTA);
        }

        return tutor;
    }

    /**
     * Indica si ya existe un tutor con el email indicado.
     *
     * @param email email a validar
     * @return {@code true} si existe una cuenta con ese email; {@code false} en caso contrario
     */
    public boolean existsByEmail(String email) {
        return tutorRepository.findByEmail(email).isPresent();
    }
}
