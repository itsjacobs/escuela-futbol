package com.escuelafutbol.ui.config;

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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth

                        // ── Páginas HTML públicas ──────────────────────────────────
                        .requestMatchers(
                                "/", "/login", "/registro", "/inscripcion",
                                "/tutor/**", "/admin/**",
                                "/css/**", "/js/**", "/images/**",
                                "/favicon.ico","/pago"
                        ).permitAll()

                        // Stripe
                        .requestMatchers("/api/stripe/**").authenticated()

                        // ── Auth API pública ───────────────────────────────────────
                        .requestMatchers("/api/auth/**").permitAll()

                        // ── Jugadores: rutas de tutor primero (orden importante) ───
                        .requestMatchers("/api/jugadores/tutor/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/jugadores").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/jugadores/{id}").authenticated()

                        // ── Jugadores: solo ADMIN ──────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/jugadores").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/jugadores/categoria/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/jugadores/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/jugadores/admin/**").hasAuthority("ADMIN")

                        // ── Pagos: rutas de tutor primero ─────────────────────────
                        .requestMatchers(HttpMethod.POST, "/api/pagos").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/pagos/jugador/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/pagos/pendiente/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/pagos/total/**").authenticated()

                        // ── Pagos: solo ADMIN ──────────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/pagos").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/pagos/**").hasAuthority("ADMIN")

                        // ── Equipaciones: rutas de tutor primero ──────────────────
                        .requestMatchers(HttpMethod.POST, "/api/equipaciones").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/equipaciones/jugador/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/equipaciones/tiene/**").authenticated()

                        // ── Equipaciones: solo ADMIN ───────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/equipaciones").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/equipaciones/**").hasAuthority("ADMIN")

                        // ── El resto requiere autenticación ───────────────────────
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}