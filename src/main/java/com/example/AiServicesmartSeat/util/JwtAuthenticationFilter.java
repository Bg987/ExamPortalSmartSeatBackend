package com.example.AiServicesmartSeat.util;

import com.example.AiServicesmartSeat.service.AuthenticationService;
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
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AuthenticationService authService;

    @Value("${SEB_CONFIG_KEY}")
    private String sebConfigKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {


        String origin = request.getHeader("Origin");
        response.setHeader("Access-Control-Allow-Origin", origin != null ? origin : "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Methods", "*");

        // 1. BYPASS OPTIONS (Pre-flight): Mandatory to avoid CORS Status 0
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        // 2. PUBLIC ROUTES: Skip filter for Login and specific Open APIs
        if ((path.equals("/api/Auth/login")) || path.contains("/public") || path.contains("/api/exam/getExamPasswordOpen")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. EXTRACT JWT FROM COOKIE
        String token = null;
        if (request.getCookies() != null) {
            token = Arrays.stream(request.getCookies())
                    .filter(cookie -> "AUTH_JWT".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        if (token == null || !jwtUtil.validateToken(token)) {
            sendErrorResponse(response, "Unauthorized: Session expired or missing.", 401);
            return;
        }

        String id = jwtUtil.extractId(token);
        String role = jwtUtil.extractRole(token);

        // 4. SEB CHECK: Apply ONLY to Student Role on Exam-Specific Endpoints
//        List<String> examPaths = List.of("/api/exam/verify", "/api/exam/sync", "/api/ExamStudent/getStudentIncomplteExam");
//        boolean isExamPath = examPaths.stream().anyMatch(path::contains);
//
//        if ("student".equalsIgnoreCase(role) && isExamPath) {
//            System.out.println("--- All Incoming Headers ---");
//            java.util.Enumeration<String> names = request.getHeaderNames();
//            while (names.hasMoreElements()) {
//                String name = names.nextElement();
//                System.out.println(name + ": " + request.getHeader(name));
//            }
//            String userAgent = request.getHeader("User-Agent");
//            // Check both cases (SEB 3.0 uses 'H')
//            String requestKey = request.getHeader("X-SafeExamBrowser-ConfigKeyhash");
//
//
//            boolean isSeb = (userAgent != null && userAgent.contains("SEB"));
//            boolean isKeyValid = (sebConfigKey != null && sebConfigKey.equals(requestKey));
//
//
//            if (!isSeb || !isKeyValid) {
//                // Perform Server-side Logout
//                authService.logout(response);
//
//                // MANUALLY ADD CORS HEADERS (Crucial for Angular to read the error)
//                origin = request.getHeader("Origin");
//                response.setHeader("Access-Control-Allow-Origin", origin != null ? origin : "https://exam-portal-smart-seat-frontend.vercel.app/");
//                response.setHeader("Access-Control-Allow-Credentials", "true");
//
//                sendErrorResponse(response, "Please use the official SeatWise SEB file and re-verify face.", 403);
//                return;
//            }
//        }

        // 5. SET SECURITY CONTEXT
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                id, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, String message, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"" + message + "\", \"status\": " + status + "}");
    }
}