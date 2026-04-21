package com.example.AiServicesmartSeat.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {


    private final SecretKey key1;
    private final SecretKey key2;

    private final HelperMethod helper;
    private static final String ALGORITHM = "AES";


    // Spring injects "sec" right here, safely
    public JwtUtil(@Value("ZmFrZVNlY3JldEtleUZha2VTZWNyZXRLZXlGYWtlU2VjcmV0") String sec1,
                   @Value("MySecretKeyForPODProjectWhichIsVeryLongAndSecure2026") String sec2, HelperMethod helper) {
        this.key1 = Keys.hmacShaKeyFor(sec1.getBytes(StandardCharsets.UTF_8));
        this.key2 = Keys.hmacShaKeyFor(sec2.getBytes(StandardCharsets.UTF_8));
        this.helper = helper;
    }



    //for smartseat backend
    public String generateToken(Long id ,String role) throws Exception {
        return Jwts.builder()
                .claim("id",helper.encrypt(id))
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 10)))
                .signWith(key1)
                .compact();
    }

    //for compiler backend
    public String generatToken2(String email,String role) {

        return Jwts.builder()
                .setSubject(email)
                .claim("role",role)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + 86400000))
                .signWith(key2, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractId(String token) {

        return String.valueOf(getClaims(token).get("id",String.class));
    }

    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key1)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key1)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }



    public boolean validateToken(String token) {
        try {
            extractAllClaims(token); // Tries to parse and verify signature
            return true; // If we get here, signature is valid
        } catch (Exception e) {
            // SignatureException, MalformedJwtException, ExpiredJwtException
            return false;
        }
    }
}