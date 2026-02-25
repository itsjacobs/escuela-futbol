    package com.escuelafutbol.domain.service;
    
    import com.escuelafutbol.data.repositories.TutorRepository;
    import com.escuelafutbol.domain.dto.TutorRegisterDTO;
    import com.escuelafutbol.domain.dto.TutorResponseDTO;
    import com.escuelafutbol.domain.model.Rol;
    import com.escuelafutbol.domain.model.Tutor;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.stereotype.Service;
    
    import java.time.LocalDateTime;
    import java.util.Optional;
    
    @Service
    public class TutorService {
        private final TutorRepository tutorRepository;
        private final PasswordEncoder passwordEncoder;
    
    
        public TutorService(TutorRepository tutorRepository, PasswordEncoder passwordEncoder) {
            this.tutorRepository = tutorRepository;
            this.passwordEncoder = passwordEncoder;
        }
    
        public TutorResponseDTO register(TutorRegisterDTO dto) {
            Tutor tutor = new Tutor();
            tutor.setNombre(dto.nombre());
            tutor.setApellidos(dto.apellidos());
            tutor.setTelefono(dto.telefono());
            tutor.setEmail(dto.email());
            tutor.setPassword(passwordEncoder.encode(dto.password()));
            tutor.setRol(Rol.TUTOR);
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
    
        public Tutor login (String email, String password){
            Tutor tutor = tutorRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    
            if (!passwordEncoder.matches(password, tutor.getPassword())) {
                throw new RuntimeException("Contraseña incorrecta");
            }
            return tutor;
        }
    
        public void delete(Tutor tutor) {
            tutorRepository.delete(tutor);
        }
        public Tutor findById(Long id) {
            return tutorRepository.findById(id).orElse(null);
        }
        public Iterable<Tutor> findAll() {
            return tutorRepository.findAll();
        }
        public long count() {
            return tutorRepository.count();
        }
        public boolean existsByEmail(String email) {
            return tutorRepository.findByEmail(email).isPresent();
        }
    }
