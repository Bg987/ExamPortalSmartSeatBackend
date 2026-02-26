package com.example.AiServicesmartSeat.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Value("${SEB_CONFIG_KEY}")
    private final String SEB_CONFIG_KEY;

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
                    .filter(cookie -> "AUTH_TOKEN".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        // 4. VALIDATE & AUTHENTICATE
        if (token != null && jwtUtil.validateToken(token)) {
            String enrollmentNo = jwtUtil.extractEnrollmentNo(token);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(enrollmentNo, null, new ArrayList<>());

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            filterChain.doFilter(request, response); // Pass to controller
        } else {
            // 5. UNAUTHORIZED: No token or expired
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Unauthorized: Please verify your face again.\"}");
        }
    }
}