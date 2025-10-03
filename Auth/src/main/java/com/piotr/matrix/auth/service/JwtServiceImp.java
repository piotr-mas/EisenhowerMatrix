package com.piotr.matrix.auth.service;

import com.piotr.matrix.auth.generated.model.JwtTokenResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Service
public class JwtServiceImp implements JwtService {

    @Value("${jwt.expiration}")//10h
    private long expiration;
    @Value("${jwt.secret}")
    private String secret;

    @Override
    public JwtTokenResponse generateToken(String username, String password, String role) {
        log.debug("Generating JWT Token for user {}", username);
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        var currentTimeMillis = new Date(System.currentTimeMillis());
        var expirationTimeMillis = new Date(System.currentTimeMillis() + expiration);
        var token = doGenerateToken(claims, username, currentTimeMillis, expirationTimeMillis);

        var jwtTokenResponse = new JwtTokenResponse(token, "Bearer");
        jwtTokenResponse.setExpiresIn(expirationTimeMillis.getTime());
        return jwtTokenResponse;
    }

    private String doGenerateToken(Map<String, Object> claims, String subject,
                                   Date currentTimeMillis, Date expirationTimeMillis) {
        log.debug("doGenerateToken subject: {}", subject);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(currentTimeMillis)
                .setExpiration(expirationTimeMillis)
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
