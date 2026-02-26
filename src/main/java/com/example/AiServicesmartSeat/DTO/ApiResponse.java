package com.example.AiServicesmartSeat.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse {
    private String status;     // "success", "fail", or "error"
    private String message;    // Human readable message
    private Boolean verified;  // For face logic
    private Double distance;   // For AI metrics
    private Object data;       // For any extra payloads
}
