package com.escuelafutbol.ui.security;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Servicio en memoria para invalidar tokens JWT.
 * <p>
 * Mantiene una blacklist de tokens revocados durante el ciclo de vida
 * de la instancia de la aplicación.
 * <p>
 * EN: In-memory JWT blacklist service used to revoke tokens.
 * ES: Servicio de blacklist en memoria para revocar tokens JWT.
 */
@Service
public class TokenBlacklistService {

    private final Set<String> blacklistedTokens = new HashSet<>();

    /**
     * Añade un token a la blacklist si tiene contenido.
     *
     * @param token token JWT a invalidar
     */
    public void blacklistToken(String token) {
        if (token != null && !token.isEmpty()) {
            blacklistedTokens.add(token);
        }
    }

    /**
     * Comprueba si un token está marcado como invalidado.
     *
     * @param token token JWT a verificar
     * @return {@code true} si está en blacklist; {@code false} en caso contrario
     */
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
}
