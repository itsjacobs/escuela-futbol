package com.escuelafutbol.ui.api;

import com.escuelafutbol.commons.Constantes;
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

/**
 * Controlador REST de autenticación y ciclo de sesión.
 * <p>
 * Expone endpoints públicos para:
 * <ul>
 *   <li>Registro de tutor</li>
 *   <li>Inicio de sesión con emisión de JWT</li>
 *   <li>Cierre de sesión mediante invalidación de token</li>
 * </ul>
 * <p>
 * EN: Provides public authentication endpoints (register/login/logout).
 * ES: Proporciona endpoints públicos de autenticación (registro/login/logout).
 */
@RestController
@RequestMapping(Constantes.RUTA_API_AUTH)
public class AuthApiController {

    private final TutorService tutorService;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Construye el controlador de autenticación con sus servicios.
     *
     * @param tutorService servicio de negocio de tutores
     * @param jwtService servicio de generación/lectura de JWT
     * @param tokenBlacklistService servicio de invalidación de tokens
     */
    public AuthApiController(TutorService tutorService, JwtService jwtService, TokenBlacklistService tokenBlacklistService) {
        this.tutorService = tutorService;
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    /**
     * Endpoint para registrar un nuevo tutor.
     * <p>
     * Validaciones relevantes:
     * <ul>
     *   <li>Validación de campos del DTO por anotaciones Bean Validation</li>
     *   <li>Control de duplicidad de email antes de persistir</li>
     * </ul>
     *
     * @param dto datos de registro del tutor
     * @return {@link ResponseEntity} con:
     * <ul>
     *   <li>{@code 201 CREATED} y tutor creado en cuerpo, si el registro es correcto</li>
     *   <li>{@code 409 CONFLICT} sin cuerpo, si el email ya está registrado</li>
     * </ul>
     */
    @PostMapping(Constantes.RUTA_AUTH_REGISTRO)
    public ResponseEntity<TutorResponseDTO> registro(@Valid @RequestBody TutorRegisterDTO dto) {
        if (tutorService.existsByEmail(dto.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        TutorResponseDTO response = tutorService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint de inicio de sesión.
     * <p>
     * Comportamiento:
     * <ol>
     *   <li>Si llega un token previo en cabecera, se invalida en blacklist</li>
     *   <li>Valida credenciales con el servicio de tutores</li>
     *   <li>Genera un nuevo JWT y devuelve datos básicos de sesión</li>
     * </ol>
     *
     * @param dto credenciales de acceso
     * @param authHeader cabecera Authorization opcional
     * @return respuesta {@code 200 OK} con token JWT, rol y nombre
     */
    @PostMapping(Constantes.RUTA_AUTH_LOGIN)
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginDTO dto,
                                                 @RequestHeader(value = Constantes.HEADER_AUTHORIZATION, required = false) String authHeader) {

        if (authHeader != null && authHeader.startsWith(Constantes.PREFIJO_BEARER)) {
            tokenBlacklistService.blacklistToken(authHeader.substring(Constantes.LONGITUD_PREFIJO_BEARER));
        }

        Tutor tutor = tutorService.login(dto.email(), dto.password());
        String token = jwtService.generateToken(tutor.getEmail(), tutor.getRol().name());
        return ResponseEntity.ok(new AuthResponseDTO(token, tutor.getRol().name(), tutor.getNombre()));
    }

    /**
     * Endpoint de cierre de sesión.
     * <p>
     * Si se proporciona token Bearer, se añade a blacklist para bloquear
     * su reutilización posterior.
     *
     * @param authHeader cabecera Authorization opcional
     * @return respuesta {@code 204 NO_CONTENT}
     */
    @PostMapping(Constantes.RUTA_AUTH_LOGOUT)
    public ResponseEntity<Void> logout(
            @RequestHeader(value = Constantes.HEADER_AUTHORIZATION, required = false) String authHeader) {

        if (authHeader != null && authHeader.startsWith(Constantes.PREFIJO_BEARER)) {
            tokenBlacklistService.blacklistToken(authHeader.substring(Constantes.LONGITUD_PREFIJO_BEARER));
        }
        return ResponseEntity.noContent().build();
    }
}
