package com.myapp.util.security.jwt;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.myapp.util.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    // Genera la clave secreta a partir de la cadena en application.properties
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // Genera el JWT a partir de la autenticación del usuario
    public String generateToken(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(Long.toString(userDetails.getIdUsuario())) // ID del Usuario como Subject
                .claim("email", userDetails.getUsername()) // Email como Claim
                .claim("roles", userDetails.getAuthorities()) // Roles como Claim
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // Obtiene el ID del usuario del token
    public Long getUserIdFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

    // Valida el token
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build().parseClaimsJws(authToken);
            
            System.out.println("JWT VALIDATION: Token válido ✔");
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException ex) {
            // Log: Token JWT inválido
             System.out.println("JWT ERROR: Firma inválida o token corrupto ❌ → " + ex.getMessage());
        } catch (ExpiredJwtException ex) {
            // Log: Token JWT expirado
            System.out.println("JWT ERROR: Token expirado ⏳ → " + ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            // Log: Token JWT no soportado
             System.out.println("JWT ERROR: Token no soportado 🚫 → " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            // Log: Cadena de claims JWT vacía
            System.out.println("JWT ERROR: Token vacío o cadena inválida ⚠️ → " + ex.getMessage());
        }
        return false;
    }
}
