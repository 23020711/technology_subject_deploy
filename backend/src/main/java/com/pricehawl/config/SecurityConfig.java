package com.pricehawl.config;

import com.pricehawl.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.*;
import org.springframework.web.cors.*;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Value("${cors.allowed-origins:}")
    private String allowedOriginsConfig;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Parse comma-separated origins from environment variable
        List<String> allowedOrigins;
        if (allowedOriginsConfig != null && !allowedOriginsConfig.trim().isEmpty()) {
            allowedOrigins = Arrays.asList(allowedOriginsConfig.split(","));
        } else {
            // Fallback for local development
            allowedOrigins = List.of(
                "http://localhost:5173",
                "http://localhost:3000",
                "http://127.0.0.1:5173"
            );
        }

        // Use setAllowedOriginPatterns when allowCredentials is true
        config.setAllowedOriginPatterns(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        // Expose headers that frontend might need
        config.setExposedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "X-Trending-Computed-At",
            "X-Trending-Next-Refresh-After",
            "X-Trending-Cache-Ttl-Seconds"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthFilter jwtFilter
    ) throws Exception {

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // Allow preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        // Swagger UI & OpenAPI
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // Public GET APIs
                        .requestMatchers(HttpMethod.GET,
                                "/products/**",
                                "/api/products/**",
                                "/api/trending-deals/**",
                                "/api/v1/price-history/**",
                                "/api/compare/**",
                                "/api/go/**",
                                "/api/recommendations/**"
                        ).permitAll()

                        // AI Chat
                        .requestMatchers(HttpMethod.POST, "/api/ai-chat/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ai-chat/**").permitAll()

                        // All other requests need authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtFilter,
                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
