package com.escuelafutbol.ui.api;

import com.escuelafutbol.domain.dto.AuthResponseDTO;
import com.escuelafutbol.domain.dto.LoginDTO;
import com.escuelafutbol.domain.dto.TutorRegisterDTO;
import com.escuelafutbol.domain.dto.TutorResponseDTO;
import com.escuelafutbol.domain.model.Tutor;
import com.escuelafutbol.domain.service.TutorService;
import com.escuelafutbol.ui.security.JwtService;
import com.escuelafutbol.ui.security.TokenBlacklistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final TutorService tutorService;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthApiController(TutorService tutorService, JwtService jwtService, TokenBlacklistService tokenBlacklistService) {
        this.tutorService = tutorService;
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/registro")
    public ResponseEntity<TutorResponseDTO> registro(@Valid @RequestBody TutorRegisterDTO dto) {
        if (tutorService.existsByEmail(dto.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        TutorResponseDTO response = tutorService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginDTO dto, @RequestHeader(value = "Authorization", required = false) String authHeader) {

        // Si viene con token previo lo invalidamos
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            tokenBlacklistService.blacklistToken(authHeader.substring(7));
        }

        try {
            Tutor tutor = tutorService.login(dto.email(), dto.password());
            String token = jwtService.generateToken(tutor.getEmail(), tutor.getRol().name());
            return ResponseEntity.ok(new AuthResponseDTO(token, tutor.getRol().name(), tutor.getNombre()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            tokenBlacklistService.blacklistToken(authHeader.substring(7));
        }
        return ResponseEntity.noContent().build();
    }
}