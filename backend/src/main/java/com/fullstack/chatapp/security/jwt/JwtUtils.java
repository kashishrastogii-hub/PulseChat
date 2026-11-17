package com.fullstack.chatapp.security.jwt;

import com.fullstack.chatapp.security.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs;

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        return Jwts.builder()
                .setSubject(principal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** Build a token directly from a username (used in tests and by the WebSocket handshake helper). */
    public String generateJwtTokenForUsername(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(secretBytes(jwtSecret));
    }

    static byte[] secretBytes(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("app.jwtSecret is not configured");
        }
        String trimmed = secret.trim();
        try {
            byte[] decoded = Decoders.BASE64.decode(trimmed);
            if (decoded.length >= 32) return decoded;
        } catch (DecodingException ignored) {
            // Plain passphrase: use its UTF-8 bytes below.
        }
        return trimmed.getBytes(StandardCharsets.UTF_8);
    }

    public String getUsernameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            logger.debug("Invalid JWT: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.debug("Expired JWT: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.debug("Unsupported JWT: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.debug("Empty JWT claims: {}", e.getMessage());
        }
        return false;
    }
}
