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

                        // ── Rutas públicas (páginas HTML) ──────────────────────────
                        .requestMatchers(
                                "/",
                                "/login",
                                "/registro",
                                "/inscripcion",
                                "/tutor/**",
                                "/admin/**",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()

                        // ── Auth API siempre pública ───────────────────────────────
                        .requestMatchers("/api/auth/**").permitAll()

                        // ── Solo ADMIN ─────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/jugadores").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/jugadores/categoria/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/jugadores/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/pagos").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/pagos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/equipaciones").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/equipaciones/**").hasRole("ADMIN")

                        // ── El resto de endpoints API requieren estar autenticado ──
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