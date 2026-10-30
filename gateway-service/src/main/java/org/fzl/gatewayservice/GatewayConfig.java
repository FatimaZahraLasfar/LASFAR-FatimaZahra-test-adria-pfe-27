package org.fzl.gatewayservice;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("account-rest", r -> r.path("/api/accounts/**")
                        .uri("lb://account-service"))
                .route("transaction-rest", r -> r.path("/api/transactions/**", "/api/transfers/**")
                        .uri("lb://transaction-service"))
                // Les deux services exposent /graphql : on les distingue côté gateway
                .route("account-graphql", r -> r.path("/graphql/accounts")
                        .filters(f -> f.setPath("/graphql"))
                        .uri("lb://account-service"))
                .route("transaction-graphql", r -> r.path("/graphql/transactions")
                        .filters(f -> f.setPath("/graphql"))
                        .uri("lb://transaction-service"))
                .route("chatbot", r -> r.path("/api/chat/**")
                        .uri("lb://chatbot-service"))
                .build();
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}
