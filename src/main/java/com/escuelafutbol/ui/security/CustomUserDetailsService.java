package com.escuelafutbol.ui.security;

import com.escuelafutbol.commons.Constantes;
import com.escuelafutbol.data.repositories.TutorRepository;
import com.escuelafutbol.domain.model.Tutor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementación de {@link UserDetailsService} basada en tutores persistidos.
 * <p>
 * Convierte entidades {@link Tutor} en objetos {@link UserDetails}
 * compatibles con Spring Security.
 * <p>
 * EN: Adapts tutor entities to Spring Security {@link UserDetails}.
 * ES: Adapta entidades de tutor al modelo {@link UserDetails} de Spring Security.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final TutorRepository tutorRepository;

    /**
     * Construye el servicio con repositorio de tutores.
     *
     * @param tutorRepository repositorio de tutores
     */
    public CustomUserDetailsService(TutorRepository tutorRepository) {
        this.tutorRepository = tutorRepository;
    }

    /**
     * Carga usuario por email para autenticación.
     *
     * @param email email/username del tutor
     * @return detalles de usuario para Spring Security
     * @throws UsernameNotFoundException si no existe tutor con ese email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Tutor tutor = tutorRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(Constantes.MENSAJE_USUARIO_NO_ENCONTRADO_CON_EMAIL + email));

        return User.builder()
                .username(tutor.getEmail())
                .password(tutor.getPassword())
                .authorities(tutor.getRol().name())
                .build();
    }
}
