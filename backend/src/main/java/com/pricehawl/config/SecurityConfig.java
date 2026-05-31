package com.pricehawl.config;

import com.pricehawl.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.*;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // IMPORTANT: Wildcard patterns like `https://*.vercel.app` DON'T work
        // with `allowCredentials(true)` in CORS - must list explicit origins
        List<String> allowedOrigins = List.of(
                "https://technology-subject-deploy.vercel.app",
                "http://localhost:5173",
                "http://localhost:5174",
                "http://localhost:8080",
                "http://127.0.0.1:5173",
                "http://127.0.0.1:8080"
        );

        config.setAllowedOriginPatterns(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
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
                .addFilterBefore(
                        jwtFilter,
                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/products/**",
                                "/api/products/**",
                                "/api/trending-deals/**",
                                "/api/v1/price-history/**",
                                "/api/compare/**",
                                "/api/go/**",
                                "/api/recommendations/**",
                                "/api/wishlist/**",
                                "/wishlist/**",
                                "/notifications/**",
                                "/api/notifications/**",
                                "/api/alerts/**",
                                "/alerts/**",
                                "/users/me",
                                "/api/users/me",
                                "/payments/**",
                                "/api/payments/**",
                                "/search/**",
                                "/api/search/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/wishlist/**",
                                "/wishlist/**",
                                "/api/notifications/**",
                                "/notifications/**",
                                "/api/alerts/**",
                                "/alerts/**",
                                "/ai-chat/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/ai-chat/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ai-chat/**").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
