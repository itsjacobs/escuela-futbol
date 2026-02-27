package com.escuelafutbol.ui.security;

import com.escuelafutbol.data.repositories.TutorRepository;
import com.escuelafutbol.domain.model.Tutor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final TutorRepository tutorRepository;

    public CustomUserDetailsService(TutorRepository tutorRepository) {
        this.tutorRepository = tutorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Tutor tutor = tutorRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        return User.builder()
                .username(tutor.getEmail())
                .password(tutor.getPassword())
                .authorities(tutor.getRol().name())
                .build();
    }
}