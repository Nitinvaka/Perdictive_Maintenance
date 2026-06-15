package com.PredictiveMaintenanceLite.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Helper class for working with JWT tokens.
 *  - generateToken() makes a token for a username
 *  - extractUsername() reads the username back out
 *  - validateToken() checks the token is valid and not expired
 */

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    // Token valid for 24 hours
    private final long EXPIRATION_MS = 1000 * 60 * 60 * 24;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(getKey())
                .compact();
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public boolean validateToken(String token, String username) {
        try {
            Claims claims = getClaims(token);
            return claims.getSubject().equals(username)
                    && claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}