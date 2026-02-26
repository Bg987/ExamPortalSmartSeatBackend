package com.example.AiServicesmartSeat.util;

import com.example.AiServicesmartSeat.DTO.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Component
public class HelperMethod {

    public ResponseEntity<ApiResponse> buildResponse(HttpStatus code, String status, String msg, Boolean verified, Double dist) {
        return ResponseEntity.status(code).body(
                ApiResponse.builder()
                        .status(status)
                        .message(msg)
                        .verified(verified)
                        .distance(dist)
                        .build()
        );
    }
}
