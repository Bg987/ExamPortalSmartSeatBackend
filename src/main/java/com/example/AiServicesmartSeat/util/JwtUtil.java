package com.example.AiServicesmartSeat.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    // IMPORTANT: In a real project, put this in application.properties
    private final String SECRET_KEY = "your-very-secret-and-very-long-key-for-seatwise-ai-2026";
    private final long EXPIRATION_TIME = 86400000; // 24 hours in milliseconds
    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // --- 1. GENERATE TOKEN ---
    public String generateToken(String enrollmentNo) {
        return Jwts.builder()
                .setSubject(enrollmentNo)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // --- 2. EXTRACT ENROLLMENT NUMBER ---
    public String extractEnrollmentNo(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // --- 3. VALIDATE TOKEN ---
    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false; // Token is tampered or malformed
        }
    }

    // --- HELPERS ---
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}