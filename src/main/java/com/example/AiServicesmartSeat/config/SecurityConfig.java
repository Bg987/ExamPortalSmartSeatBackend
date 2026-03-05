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
import org.springframework.web.cors.CorsConfiguration;
import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final forbiddenHandler myForbiddenHandler;
    private final CorsConfiguration corsConfiguration;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disable CSRF (not needed for Stateless JWT APIs)
                .csrf(csrf -> csrf.disable())

                // 2. Configure CORS (Must allow your Angular URL)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. Set Session to Stateless (Don't store sessions on server)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. Define Route Permissions
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/ExamApi/Auth/**").permitAll() // Open to everyone
                        //.requestMatchers("/api/ai/questions/**").authenticated()
                        .anyRequest().authenticated() // Everything else needs a valid JWT
                )

                // 5. THE MAGIC LINE: Put your JWT filter before the default one
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(myForbiddenHandler));
        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
        config.setAllowCredentials(true); // CRITICAL: Allows cookies to be sent
        config.setAllowedOrigins(java.util.List.of("http://localhost:4200","https://smart-seat-frontend-three.vercel.app")); // Your Angular URL
        config.setAllowedHeaders(java.util.List.of("Origin", "Content-Type", "Accept", "Authorization"));
        config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}