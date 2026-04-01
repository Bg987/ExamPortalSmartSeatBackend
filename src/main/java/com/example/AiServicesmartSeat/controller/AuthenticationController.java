package com.example.AiServicesmartSeat.controller;


import com.example.AiServicesmartSeat.DTO.ApiResponse;
import com.example.AiServicesmartSeat.DTO.LogoutRequest;
import com.example.AiServicesmartSeat.entity.BlockSession;
import com.example.AiServicesmartSeat.repository.BlockSessionRepository;
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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/Auth")
public class AuthenticationController {

    private final AuthenticationService authService;
    private final HelperMethod helper;
    private final BlockSessionRepository blockSessionRepository;
    private final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");//for deployment

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestParam("enrollmentNumber") String enrollmentNumber,
                                             @RequestParam("image") MultipartFile image,
                                             HttpServletResponse res,
                                             Authentication authentication) {

        try {
            String blockMessage = authService.checkBlockStatus(enrollmentNumber);

            if (blockMessage != null) {

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.builder()
                        .status("blocked")
                        .message(blockMessage)
                        .build());
            }

            return (ResponseEntity<ApiResponse>) authService.faceAuth(enrollmentNumber, image,res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.builder()
                    .status("error")
                    .message("Internal Server Error: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody LogoutRequest request, HttpServletResponse response){

        if (Boolean.TRUE.equals(request.getIsViolation())) {
            String enrNumber = helper.getEnrNumberIdByUserId();
            BlockSession block = new BlockSession(enrNumber, ZonedDateTime.now(IST_ZONE).toLocalDateTime());
            blockSessionRepository.save(block);

        }

        return authService.logout(response);
    }

}