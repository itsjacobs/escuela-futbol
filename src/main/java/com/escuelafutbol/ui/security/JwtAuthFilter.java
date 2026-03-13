package com.escuelafutbol.ui.security;

import com.escuelafutbol.commons.Constantes;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticación JWT ejecutado una vez por petición.
 * <p>
 * Flujo principal:
 * <ol>
 *   <li>Lee cabecera Authorization</li>
 *   <li>Valida formato Bearer y estado de blacklist</li>
 *   <li>Extrae usuario del token y carga detalles</li>
 *   <li>Registra autenticación en {@link SecurityContextHolder}</li>
 * </ol>
 * <p>
 * EN: JWT request filter that authenticates users and populates security context.
 * ES: Filtro JWT que autentica usuarios y rellena el contexto de seguridad.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Construye el filtro con los servicios de JWT, usuarios y blacklist.
     *
     * @param jwtService servicio JWT
     * @param userDetailsService servicio de carga de usuarios
     * @param tokenBlacklistService servicio de invalidación de tokens
     */
    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService, TokenBlacklistService tokenBlacklistService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    /**
     * Procesa la autenticación JWT de la petición HTTP actual.
     * <p>
     * En caso de token inválido, expirado o malformado responde con 401 en JSON.
     *
     * @param request petición HTTP entrante
     * @param response respuesta HTTP saliente
     * @param filterChain cadena de filtros
     * @throws ServletException si ocurre error de servlet durante el filtrado
     * @throws IOException si ocurre error de E/S al escribir respuesta
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader(Constantes.HEADER_AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(Constantes.PREFIJO_BEARER)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(Constantes.LONGITUD_PREFIJO_BEARER);

        if (jwt.trim().isEmpty()) {
            writeUnauthorized(response, Constantes.MENSAJE_TOKEN_VACIO);
            return;
        }

        if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
            writeUnauthorized(response, Constantes.MENSAJE_TOKEN_INVALIDADO);
            return;
        }

        try {
            final String username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    log.info("Usuario autenticado: {} | Authorities: {}",
                            userDetails.getUsername(),
                            userDetails.getAuthorities());

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

        } catch (ExpiredJwtException e) {
            log.warn("Token expirado: {}", e.getMessage());
            writeUnauthorized(response, Constantes.MENSAJE_TOKEN_EXPIRADO);
            return;
        } catch (MalformedJwtException e) {
            log.warn("Token malformado: {}", e.getMessage());
            writeUnauthorized(response, Constantes.MENSAJE_TOKEN_MALFORMADO);
            return;
        } catch (Exception e) {
            log.error("Error en autenticación JWT: {}", e.getMessage(), e);
            writeUnauthorized(response, Constantes.MENSAJE_ERROR_AUTENTICACION);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Escribe una respuesta 401 con cuerpo JSON de error.
     *
     * @param response respuesta HTTP
     * @param message mensaje de error funcional
     * @throws IOException si ocurre error al escribir el cuerpo
     */
    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(String.format(Constantes.JSON_ERROR_TEMPLATE, message));
    }
}
