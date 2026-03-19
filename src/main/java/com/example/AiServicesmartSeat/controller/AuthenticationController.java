package com.example.AiServicesmartSeat.controller;


import com.example.AiServicesmartSeat.DTO.ApiResponse;
import com.example.AiServicesmartSeat.service.AuthenticationService;
import com.example.AiServicesmartSeat.util.HelperMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/Auth")
public class AuthenticationController {

    private final AuthenticationService authService;
    private final HelperMethod helper;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestParam("enrollmentNumber") String enrollmentNumber,
                                             @RequestParam("image") MultipartFile image,
                                             HttpServletResponse res,
                                             Authentication authentication) {

        try {
            return (ResponseEntity<ApiResponse>) authService.faceAuth(enrollmentNumber, image,res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.builder()
                    .status("error")
                    .message("Internal Server Error: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response){
        return authService.logout(response);
    }

}