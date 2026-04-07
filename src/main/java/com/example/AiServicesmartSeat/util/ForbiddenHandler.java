package com.example.AiServicesmartSeat.util;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class ForbiddenHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        // 1. Manually add CORS headers
        // This ensures Angular can read the 403 error instead of throwing a CORS "Status 0"
        String origin = request.getHeader("Origin");
        response.setHeader("Access-Control-Allow-Origin", origin != null ? origin : "http://localhost:4201");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        // 2. Set Response Metadata
        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
        response.setContentType("application/json");

        // 3. Create a clean JSON error body for SeatWise AI
        String jsonResponse = String.format(
                "{\"timestamp\": \"%s\", \"status\": 403, \"error\": \"Forbidden\", \"message\": \"Access Denied: You do not have permission to access this resource or you are not using Safe Exam Browser.\"}",
                LocalDateTime.now()
        );

        response.getWriter().write(jsonResponse);
    }
}