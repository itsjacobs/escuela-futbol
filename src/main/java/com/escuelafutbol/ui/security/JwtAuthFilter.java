package com.escuelafutbol.ui.security;

import com.escuelafutbol.commons.Constantes;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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

        final String jwt = resolveToken(request);

        if (jwt == null || jwt.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
            log.info("Token en blacklist detectado; se limpia cookie y se continua sin autenticar");
            clearJwtCookie(request, response);
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
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
            clearJwtCookie(request, response);
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        } catch (MalformedJwtException e) {
            log.warn("Token malformado: {}", e.getMessage());
            clearJwtCookie(request, response);
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        } catch (Exception e) {
            log.error("Error en autenticación JWT: {}", e.getMessage(), e);
            clearJwtCookie(request, response);
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Resuelve token JWT desde cabecera Authorization o cookie de sesión.
     *
     * @param request petición HTTP
     * @return token JWT o {@code null} si no está presente
     */
    private String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader(Constantes.HEADER_AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(Constantes.PREFIJO_BEARER)) {
            String token = authHeader.substring(Constantes.LONGITUD_PREFIJO_BEARER);
            if (token.trim().isEmpty()) {
                return null;
            }
            return token;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (Constantes.COOKIE_JWT_TOKEN.equals(cookie.getName())) {
                String token = cookie.getValue();
                if (token == null || token.trim().isEmpty()) {
                    return null;
                }
                return token;
            }
        }

        return null;
    }


    /**
     * Limpia la cookie JWT para evitar bucles de token inválido tras reabrir navegador.
     */
    private void clearJwtCookie(HttpServletRequest request, HttpServletResponse response) {
        ResponseCookie deleteCookie = ResponseCookie.from(Constantes.COOKIE_JWT_TOKEN, Constantes.CADENA_VACIA)
                .httpOnly(true)
                .secure(request.isSecure())
                .path(Constantes.COOKIE_PATH_ROOT)
                .sameSite(Constantes.COOKIE_SAMESITE_LAX)
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
    }
}
