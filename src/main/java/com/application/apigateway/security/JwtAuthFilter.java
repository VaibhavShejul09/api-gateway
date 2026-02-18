package com.application.apigateway.security;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthFilter implements GatewayFilter {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_ROLE = "X-Role";

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        // 1️⃣ Public endpoints
        if (path.startsWith("/api/auth")) {
            return chain.filter(exchange);
        }

        // 2️⃣ Allow CORS preflight
        if ("OPTIONS".equals(exchange.getRequest().getMethod().name())) {
            return chain.filter(exchange);
        }

        // 3️⃣ Read Authorization header
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);

        // 4️⃣ Validate token
        if (!jwtUtil.isTokenValid(token)) {
            return unauthorized(exchange);
        }

        Claims claims = jwtUtil.extractClaims(token);

        // 5️⃣ Extract REQUIRED claims
        String userId = claims.getSubject(); // ✅ UUID string
        String role = claims.get("role", String.class); // ROLE_USER

        // 6️⃣ Forward ONLY trusted user context
        ServerWebExchange mutatedExchange =
                exchange.mutate()
                        .request(request -> request.headers(headers -> {
                            headers.set(HEADER_USER_ID, userId);
                            headers.set(HEADER_ROLE, role);
                        }))
                        .build();

        return chain.filter(mutatedExchange);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
