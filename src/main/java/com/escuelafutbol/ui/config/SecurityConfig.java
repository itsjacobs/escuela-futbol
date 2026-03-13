package com.escuelafutbol.ui.config;

import com.escuelafutbol.commons.Constantes;
import com.escuelafutbol.ui.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración central de seguridad de la aplicación.
 * <p>
 * Define:
 * <ul>
 *   <li>Reglas de autorización por rutas y método HTTP</li>
 *   <li>Política stateless basada en JWT</li>
 *   <li>Proveedor de autenticación con {@link UserDetailsService}</li>
 *   <li>Integración del filtro {@link JwtAuthFilter}</li>
 * </ul>
 * <p>
 * EN: Central Spring Security configuration for route protection and JWT setup.
 * ES: Configuración central de Spring Security para protección de rutas y JWT.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    /**
     * Construye la configuración de seguridad con dependencias inyectadas.
     *
     * @param jwtAuthFilter filtro JWT para autenticación por token
     * @param userDetailsService servicio de carga de usuarios
     */
    public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Define la cadena de filtros de seguridad HTTP.
     * <p>
     * Configuración aplicada:
     * <ul>
     *   <li>CSRF deshabilitado para API stateless</li>
     *   <li>Autorización por rutas públicas, tutor y admin</li>
     *   <li>Sesión sin estado ({@link SessionCreationPolicy#STATELESS})</li>
     *   <li>Registro del filtro JWT antes del filtro de usuario/contraseña</li>
     * </ul>
     *
     * @param http objeto de configuración de seguridad HTTP
     * @return cadena de filtros de seguridad configurada
     * @throws Exception si ocurre un error al construir la configuración
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                Constantes.RUTA_ROOT, Constantes.RUTA_LOGIN, Constantes.RUTA_REGISTRO, Constantes.RUTA_INSCRIPCION,
                                Constantes.RUTA_CSS_ALL, Constantes.RUTA_JS_ALL, Constantes.RUTA_IMAGES_ALL,
                                Constantes.RUTA_FAVICON, Constantes.RUTA_PAGO, Constantes.RUTA_TRABAJA
                        ).permitAll()

                        .requestMatchers(Constantes.RUTA_TUTOR_ALL).hasAnyAuthority(Constantes.ROL_TUTOR, Constantes.AUTHORITY_ADMIN)
                        .requestMatchers(Constantes.RUTA_ADMIN_ALL).hasAuthority(Constantes.AUTHORITY_ADMIN)

                        .requestMatchers(Constantes.RUTA_API_AUTH_ALL).permitAll()

                        .requestMatchers(Constantes.RUTA_API_JUGADORES + Constantes.RUTA_TUTOR_ALL).authenticated()
                        .requestMatchers(HttpMethod.POST, Constantes.RUTA_API_JUGADORES).authenticated()
                        .requestMatchers(HttpMethod.GET, Constantes.RUTA_API_JUGADORES_ID).authenticated()

                        .requestMatchers(HttpMethod.GET, Constantes.RUTA_API_JUGADORES).hasAuthority(Constantes.AUTHORITY_ADMIN)
                        .requestMatchers(HttpMethod.GET, Constantes.RUTA_API_JUGADORES_CATEGORIA_ALL).hasAuthority(Constantes.AUTHORITY_ADMIN)
                        .requestMatchers(HttpMethod.DELETE, Constantes.RUTA_API_JUGADORES_ALL).hasAuthority(Constantes.AUTHORITY_ADMIN)
                        .requestMatchers(Constantes.RUTA_API_JUGADORES + Constantes.RUTA_ADMIN_ALL).hasAuthority(Constantes.AUTHORITY_ADMIN)

                        .requestMatchers(HttpMethod.POST, Constantes.RUTA_API_PAGOS + Constantes.RUTA_PAGOS_EFECTIVO).hasAuthority(Constantes.AUTHORITY_ADMIN)
                        .requestMatchers(HttpMethod.POST, Constantes.RUTA_API_PAGOS).authenticated()
                        .requestMatchers(HttpMethod.GET, Constantes.RUTA_API_PAGOS_JUGADOR_ALL).authenticated()
                        .requestMatchers(HttpMethod.GET, Constantes.RUTA_API_PAGOS_PENDIENTE_ALL).authenticated()
                        .requestMatchers(HttpMethod.GET, Constantes.RUTA_API_PAGOS_TOTAL_ALL).authenticated()
                        .requestMatchers(HttpMethod.POST, Constantes.RUTA_API_PAGOS + Constantes.RUTA_PAGOS_ELEGIR_CUOTAS).authenticated()


                        .requestMatchers(HttpMethod.GET, Constantes.RUTA_API_PAGOS).hasAuthority(Constantes.AUTHORITY_ADMIN)
                        .requestMatchers(HttpMethod.DELETE, Constantes.RUTA_API_PAGOS_ALL).hasAuthority(Constantes.AUTHORITY_ADMIN)
                        .requestMatchers(HttpMethod.GET, Constantes.RUTA_API_PAGOS + Constantes.RUTA_PAGOS_PENDIENTES).hasAuthority(Constantes.AUTHORITY_ADMIN)
                        .requestMatchers(HttpMethod.PUT, Constantes.RUTA_API_PAGOS_CONFIRMAR).hasAuthority(Constantes.AUTHORITY_ADMIN)
                        .requestMatchers(HttpMethod.PUT, Constantes.RUTA_API_PAGOS_RECHAZAR).hasAuthority(Constantes.AUTHORITY_ADMIN)

                        .requestMatchers(HttpMethod.POST, Constantes.RUTA_API_EQUIPACIONES).authenticated()
                        .requestMatchers(HttpMethod.GET, Constantes.RUTA_API_EQUIPACIONES_JUGADOR_ALL).authenticated()
                        .requestMatchers(HttpMethod.GET, Constantes.RUTA_API_EQUIPACIONES_TIENE_ALL).authenticated()

                        .requestMatchers(HttpMethod.GET, Constantes.RUTA_API_EQUIPACIONES).hasAuthority(Constantes.AUTHORITY_ADMIN)
                        .requestMatchers(HttpMethod.DELETE, Constantes.RUTA_API_EQUIPACIONES_ALL).hasAuthority(Constantes.AUTHORITY_ADMIN)

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Crea el proveedor de autenticación basado en DAO.
     *
     * @return proveedor de autenticación con password encoder BCrypt
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Expone el {@link AuthenticationManager} de Spring Security.
     *
     * @param config configuración de autenticación de Spring
     * @return administrador de autenticación
     * @throws Exception si no puede resolverse el manager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Define el codificador de contraseñas por defecto.
     *
     * @return instancia de {@link BCryptPasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
