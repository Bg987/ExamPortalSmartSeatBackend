package com.example.AiServicesmartSeat.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Value("${SEB_CONFIG_KEY}")
    private  String SEB_CONFIG_KEY;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. SEB CHECK: Every request must come from SEB
//        String userAgent = request.getHeader("User-Agent");
//        String requestConfigKey = request.getHeader("X-SafeExamBrowser-ConfigKeyhash");
//        boolean isSeb = (userAgent != null && userAgent.contains("SEB"));
//        boolean isKeyValid = (requestConfigKey != null && requestConfigKey.equals(ALLOWED_CONFIG_KEY));

//        if (!isSeb || !isKeyValid) {
//            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//            response.setContentType("application/json");
//            response.getWriter().write("{\"error\": \"Unauthorized: Please use the official SeatWise SEB file.\"}");
//            return; // Stop here!
//        }

        // 2. PUBLIC PATH CHECK: Allow login without a JWT token
        String path = request.getServletPath();
        if (path.contains("/ExamApi/Auth/login") || path.contains("/public")) {
            filterChain.doFilter(request, response);
            return; // Stop processing this filter, move to the next
        }

        // 3. JWT CHECK: Extract the "AUTH_TOKEN" cookie
        String token = null;
        if (request.getCookies() != null) {
            token = Arrays.stream(request.getCookies())
                    .filter(cookie -> "AUTH_JWT".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{ \"error\": \"Access denied\", \"message\": \"Cookie modified or expired. Login again.\" }"
            );
            return;
        }

        String id = jwtUtil.extractId(token);
        String role = jwtUtil.extractRole(token);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        id,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );

        SecurityContextHolder.getContext().setAuthentication(auth);

        // Continue filter chain
        filterChain.doFilter(request, response);

    }
}