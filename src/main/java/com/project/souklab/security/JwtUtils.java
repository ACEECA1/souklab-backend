package com.project.souklab.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.project.souklab.config.AppProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtUtils {

    private final AppProperties appProperties;

    private Key getSigningKey() {
        byte[] keyBytes = appProperties.getJwt().getSecret().getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof UserDetails userPrincipal)) {
            throw new IllegalArgumentException("Expected principal of type UserDetails, but found: "
                    + (authentication.getPrincipal() != null ? authentication.getPrincipal().getClass().getName() : "null"));
        }
        return generateTokenFromUsername(userPrincipal.getUsername(), appProperties.getJwt().getAccessTokenExpirationMs());
    }

    public String generateAccessToken(String username) {
        return generateTokenFromUsername(username, appProperties.getJwt().getAccessTokenExpirationMs());
    }

    public String generateRefreshToken(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof UserDetails userPrincipal)) {
            throw new IllegalArgumentException("Expected principal of type UserDetails, but found: "
                    + (authentication.getPrincipal() != null ? authentication.getPrincipal().getClass().getName() : "null"));
        }
        return generateTokenFromUsername(userPrincipal.getUsername(), appProperties.getJwt().getRefreshTokenExpirationMs());
    }

    public String generateTokenFromUsername(String username, long expirationMs) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
            return true;
        } catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
