package com.shoppingcart.gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * The single place a token is checked. Every service behind this gateway trusts the two headers
 * added here, so this filter is the boundary: nothing reaches a service without passing it.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtValidator jwtValidator;
    private final RouteRules routeRules;

    public JwtAuthenticationFilter(JwtValidator jwtValidator, RouteRules routeRules) {
        this.jwtValidator = jwtValidator;
        this.routeRules = routeRules;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        var method = request.getMethod();
        var path = request.getPath().pathWithinApplication();

        if (routeRules.isPublic(method, path)) {
            return chain.filter(exchange);
        }

        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return deny(exchange, HttpStatus.UNAUTHORIZED, "missing or malformed Authorization header");
        }

        Claims claims;
        try {
            claims = jwtValidator.parse(header.substring(7));
        } catch (JwtException | IllegalArgumentException e) {
            return deny(exchange, HttpStatus.UNAUTHORIZED, "invalid or expired token");
        }

        String userId = claims.getSubject();
        String role = String.valueOf(claims.get("role"));

        if (!routeRules.isAllowed(method, path, role)) {
            return deny(exchange, HttpStatus.FORBIDDEN, "role " + role + " cannot access this endpoint");
        }

        // The services read these two and never parse a token themselves. Set, not add, so a
        // caller cannot smuggle their own X-User-Id in and be believed.
        ServerHttpRequest mutated = request.mutate()
                .header("X-User-Id", userId)
                .header("X-User-Role", role)
                .build();

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private Mono<Void> deny(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"status\":" + status.value()
                + ",\"message\":\"" + message
                + "\",\"timestamp\":\"" + LocalDateTime.now() + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /** Ahead of the routing filter, so an unauthorised call never reaches a service. */
    @Override
    public int getOrder() {
        return -1;
    }
}
