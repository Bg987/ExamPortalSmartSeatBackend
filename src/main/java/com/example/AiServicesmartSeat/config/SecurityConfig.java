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
                        .requestMatchers("/api/Auth/**","/api/exam/getExamPasswordOpen/**").permitAll() // Open to everyone
                        //.requestMatchers("/api/ai/questions/**").authenticated()
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jFiler, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception.accessDeniedHandler(myForbiddenHandler));

        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();

        config.setAllowCredentials(true);

        config.setAllowedOrigins(java.util.List.of(
                "http://localhost:4200",
                "http://localhost:4201",
                "https://smart-seat-frontend-three.vercel.app",
                "http://localhost:8080",//for telegram chatbot server1 local url
                "https://smartseatbackend.onrender.com",//same as above
                "https://exam-portal-smart-seat-frontend.vercel.app",
                "https://proxy-0xaq.onrender.com"
        ));

        // Add X-Requested-With and the SEB header to allowed headers
        config.setAllowedHeaders(java.util.List.of(
                "Origin",
                "Content-Type",
                "Accept",
                "Authorization",
                "X-Requested-With",
                "X-SafeExamBrowser-ConfigKeyhash"
        ));

        config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Ensure the browser can see these headers in the response
        config.setExposedHeaders(java.util.List.of("Set-Cookie", "Authorization"));

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}