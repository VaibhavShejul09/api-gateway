package com.application.apigateway.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoggingFilter implements GatewayFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Use getMethod().name() instead of getMethodValue()
        String method = exchange.getRequest().getMethod() != null
                ? exchange.getRequest().getMethod().name()
                : "UNKNOWN";
        String path = exchange.getRequest().getPath().toString();
        String query = exchange.getRequest().getQueryParams().toString();

        log.info("[GATEWAY REQUEST] {} {} {}", method, path, query);

        return chain.filter(exchange)
                .doOnError(throwable -> log.error("[GATEWAY ERROR] {} {} -> {}", method, path, throwable.getMessage()))
                .doFinally(signalType -> {
                    int status = exchange.getResponse().getStatusCode() != null
                            ? exchange.getResponse().getStatusCode().value()
                            : 0;
                    log.info("[GATEWAY RESPONSE] {} {} -> Status: {}", method, path, status);
                });

    }
}
