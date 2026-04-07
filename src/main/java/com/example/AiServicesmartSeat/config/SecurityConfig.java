package com.example.AiServicesmartSeat.config;

import com.example.AiServicesmartSeat.util.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import com.example.AiServicesmartSeat.util.ForbiddenHandler;

@Configuration
@EnableWebSecurity // CRITICAL: This enables the security chain
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ForbiddenHandler myForbiddenHandler; // Ensure the class name starts with Uppercase
    private final JwtAuthenticationFilter jFiler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CORS Configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. CSRF & Frame Options (Disabled for APIs and H2/Proxies)
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                // 3. Stateless Session (JWT)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. Request Authorization
                .authorizeHttpRequests(auth -> auth
                        // Allow all Pre-flight (OPTIONS) requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public Endpoints
                        .requestMatchers("/api/Auth/**", "/api/exam/getExamPasswordOpen/**").permitAll()

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                // 5. Exception Handling
                .exceptionHandling(exception -> exception.accessDeniedHandler(myForbiddenHandler))

                // 6. Add Custom Filter BEFORE the standard Auth filter
                .addFilterBefore(jFiler, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();

        config.setAllowCredentials(true);

        // Allowed Origins
        config.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "http://localhost:4201",
                "http://localhost:8081",
                "https://smart-seat-frontend-three.vercel.app",
                "https://exam-portal-smart-seat-frontend.vercel.app",
                "https://proxy-0xaq.onrender.com",
                "https://smartseatbackend.onrender.com"
        ));

        // Allowed Headers (Including ALL SEB specific headers)
        config.setAllowedHeaders(List.of(
                "Origin",
                "Content-Type",
                "Accept",
                "Authorization",
                "X-Requested-With",
                "User-Agent",
                "X-SafeExamBrowser-ConfigKeyHash",
                "X-SafeExamBrowser-RequestHash",
                "X-SafeExamBrowser-BrowserExamKey",
                "Cache-Control",
                "Pragma"
        ));

        // Allowed Methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Headers exposed to the browser/client (Important for SEB validation)
        config.setExposedHeaders(List.of(
                "Set-Cookie",
                "Authorization",
                "X-SafeExamBrowser-ConfigKeyHash",
                "X-SafeExamBrowser-RequestHash",
                "X-SafeExamBrowser-BrowserExamKey"
        ));

        // Apply preflight cache duration
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}