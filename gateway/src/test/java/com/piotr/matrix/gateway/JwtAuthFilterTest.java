package com.piotr.matrix.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.swing.*;

import java.nio.charset.StandardCharsets;
import java.security.Key;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith( SpringExtension.class)
class JwtAuthFilterTest {

    private JwtAuthFilter filter;
    private static final String secret = "dGhpc19pcyBhX2NvbXBsZXRlbHlfcmFuZG9tX2tleV8xMjM0NTY3";
    private Key signingKey;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthFilter(secret);
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    @Test
    void shouldAllowRequestWithValidToken() {
        Claims claims = Jwts.claims().setSubject("user123");
        claims.put("roles", "ADMIN");

        String token = Jwts.builder()
                .setClaims(claims)
                .signWith(signingKey)
                .compact();

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/users/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();

        ServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain).filter(any());
    }

    @Test
    void shouldRejectRequestWithMissingToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/users/1").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();
        // Add an assertion to check the side effect (the response status code)
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode(),
                "The response status should be UNAUTHORIZED");

        // Ensure the filter chain was NOT called
        verify(chain, never()).filter(any());
    }

}