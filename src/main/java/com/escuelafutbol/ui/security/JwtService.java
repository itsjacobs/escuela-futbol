package com.escuelafutbol.ui.security;

import com.escuelafutbol.commons.Constantes;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * Servicio de utilidades JWT para autenticación stateless.
 * <p>
 * Proporciona operaciones de emisión de token, extracción de claims
 * y validación de integridad/expiración.
 * <p>
 * EN: Provides JWT creation, claim extraction, and token validation helpers.
 * ES: Proporciona utilidades para crear JWT, extraer claims y validar tokens.
 */
@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;


    /**
     * Extrae el username (subject) contenido en el token.
     *
     * @param token token JWT firmado
     * @return subject/username contenido en el token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae un claim concreto del token mediante función de mapeo.
     *
     * @param token token JWT firmado
     * @param claimsResolver función que transforma el objeto claims al tipo deseado
     * @param <T> tipo de dato del claim resultante
     * @return valor resuelto del claim solicitado
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Genera un token JWT estándar para usuario y rol.
     *
     * @param username identificador del usuario autenticado
     * @param rol rol del usuario para incluir como claim
     * @return token JWT firmado
     */
    public String generateToken(String username, String rol) {
        return generateToken(Map.of(Constantes.CLAIM_ROL, rol), username);
    }

    /**
     * Genera token JWT incorporando claims adicionales.
     *
     * @param extraClaims claims personalizados
     * @param username subject del token
     * @return token JWT firmado
     */
    public String generateToken(Map<String, Object> extraClaims, String username) {
        return buildToken(extraClaims, username, jwtExpiration);
    }


    /**
     * Construye y firma un token con expiración configurable.
     *
     * @param extraClaims claims personalizados
     * @param username subject del token
     * @param expiration tiempo de validez en milisegundos
     * @return token JWT firmado y serializado
     */
    public String buildToken(Map<String, Object> extraClaims, String username, long expiration) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Verifica si el token pertenece al usuario y no está expirado.
     *
     * @param token token JWT
     * @param userDetails usuario de referencia
     * @return {@code true} si el token es válido para ese usuario
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String usernameToken = extractUsername(token);
        return (usernameToken.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Comprueba si el token ha expirado.
     *
     * @param token token JWT
     * @return {@code true} si la expiración ya se alcanzó
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrae la fecha de expiración del token.
     *
     * @param token token JWT
     * @return fecha de expiración
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Parsea y devuelve todos los claims del token firmado.
     *
     * @param token token JWT
     * @return claims contenidos en el token
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith((SecretKey) getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Devuelve la clave de firma HMAC derivada del secreto configurado.
     *
     * @return clave criptográfica de firma/verificación
     */
    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
