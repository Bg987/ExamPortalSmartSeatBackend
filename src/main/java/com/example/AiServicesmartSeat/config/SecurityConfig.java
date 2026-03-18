package com.example.AiServicesmartSeat.config;

import com.example.AiServicesmartSeat.util.JwtAuthenticationFilter;
import jakarta.servlet.DispatcherType;
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
import org.springframework.web.cors.CorsConfiguration;
import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final forbiddenHandler myForbiddenHandler;
    private final JwtAuthenticationFilter jFiler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http

                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable()) // Typical for proxies
                )
                .csrf(csrf -> csrf.disable()) // Disable CSRF for JWT
                // Link directly to the bean defined below
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/ExamApi/Auth/**").permitAll() // Open to everyone
                        //.requestMatchers("/api/ai/questions/**").authenticated()
                        .requestMatchers("/api/**").authenticated()
                )

                .addFilterBefore(jFiler, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception.accessDeniedHandler(myForbiddenHandler));

        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();

        config.setAllowCredentials(true);

        // Combined all your origins here (NO trailing slashes)
        config.setAllowedOrigins(java.util.List.of(
                "http://localhost:4201",
                "http://localhost:4200",
                "https://proxy-0xaq.onrender.com/api2", // Your main Vercel URL
                "https://exam-portal-smart-seat-frontend.vercel.app"
        ));

        config.setAllowedHeaders(java.util.List.of("Origin", "Content-Type", "Accept", "Authorization", "Set-Cookie"));
        config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setExposedHeaders(java.util.List.of("Authorization", "Set-Cookie"));

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}