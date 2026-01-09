package com.piotr.matrix.gateway;

import com.piotr.matrix.gateway.exception.GatewayException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Log4j2
@Component
public class JwtAuthFilter implements GlobalFilter {

    private final JwtParser jwtParser;

    public JwtAuthFilter(@Value("${jwt.secret}") String secret) {
        // Decode the Base64 String to bytes
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.jwtParser = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(keyBytes))
                .build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        log.info("path: {}", path);
        if (path.endsWith("/login")) {
            log.debug("Path {} is excluded from JWT check.", path);
            return chain.filter(exchange); // Skip JWT check for excluded paths
        }

        var pathSegments = path.split("/");
        var requestedId = pathSegments.length > 1 ? pathSegments[pathSegments.length - 1] : null;
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        log.info("authHeader: {}", authHeader);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("Missing or invalid Authorization header for path: {}", path);
            throw new GatewayException(401, "Missing or invalid Authorization header for path: " + path);
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtParser.parseClaimsJws(token).getBody();
            String userRoles = claims.get("role", String.class);

            if (Objects.equals(userRoles, "user") && !claims.getSubject().equals(requestedId)) {
                throw new GatewayException(403, "User not authorized to perform this operation");
            }
            // Optionally forward claims to downstream services
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", claims.getSubject())
                    .header("X-User-Roles", claims.get("role", String.class))
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (JwtException _) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
}
