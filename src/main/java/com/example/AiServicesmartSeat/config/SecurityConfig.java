package com.example.AiServicesmartSeat.config;

import com.example.AiServicesmartSeat.util.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disable CSRF (not needed for Stateless JWT APIs)
                .csrf(csrf -> csrf.disable())

                // 2. Configure CORS (Must allow your Angular URL)
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));
                    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(Collections.singletonList("*"));
                    config.setAllowCredentials(true); // REQUIRED for HttpOnly Cookies
                    return config;
                }))

                // 3. Set Session to Stateless (Don't store sessions on server)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. Define Route Permissions
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Add this!
                        .requestMatchers("/ExamApi/Auth/login").permitAll() // Open to everyone
                        .requestMatchers("/api/ai/questions/**").authenticated()
                        .anyRequest().authenticated() // Everything else needs a valid JWT
                )

                // 5. THE MAGIC LINE: Put your JWT filter before the default one
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}