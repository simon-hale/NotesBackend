package org.projects.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    private final List<String> allowedOrigins;

    public CorsConfig(
            @Value("${app.cors.allowed-origins}")
            String allowedOrigins) {

        this.allowedOrigins = Arrays.stream(
                        allowedOrigins.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();

        if (this.allowedOrigins.isEmpty()) {
            throw new IllegalArgumentException(
                    "app.cors.allowed-origins cannot be empty"
            );
        }
    }

    @Bean
    public CorsFilter corsFilter() {

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        CorsConfiguration cors =
                new CorsConfiguration();

        cors.setAllowCredentials(true);

        cors.setAllowedOrigins(allowedOrigins);

        cors.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-XSRF-TOKEN",
                "X-Requested-With"
        ));

        cors.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
        ));

        cors.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", cors);

        return new CorsFilter(source);
    }
}