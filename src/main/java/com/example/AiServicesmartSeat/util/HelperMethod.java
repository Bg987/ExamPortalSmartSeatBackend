package com.example.AiServicesmartSeat.util;

import com.example.AiServicesmartSeat.DTO.ApiResponse;
import com.example.AiServicesmartSeat.entity.College;
import com.example.AiServicesmartSeat.repository.CollegeRepository;
import com.example.AiServicesmartSeat.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class HelperMethod {

    private final StudentRepository stuRepo;
    private final CollegeRepository collegeRepo;

    public ResponseEntity<ApiResponse> buildResponse(HttpStatus code, String status, String msg, Boolean verified, Double dist,String token) {
        return ResponseEntity.status(code).body(
                ApiResponse.builder()
                        .status(status)
                        .message(msg)
                        .verified(verified)
                        .distance(dist)
                        .data(token)
                        .build()
        );
    }

    public String getEnrNumberIdByUserId() {
        String studentId = getId();
        return stuRepo.findEnrollmentNoByStudentId(Long.parseLong(studentId)) // Or the method we fixed earlier
                .orElseThrow(() -> new RuntimeException("enr. number not found for student ID: " + studentId));
    }

    public String getId(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // 2. Extract the Principal (which is "752" in your case)
        return auth.getPrincipal().toString();
    }

    public Long getCollegeIdByUserId() {
        String userId= getId();
        return collegeRepo.findByUser_userId(Long.parseLong(userId)) // Or the method we fixed earlier
                .map(College::getCollegeId)
                .orElseThrow(() -> new RuntimeException("College not found for User ID: " + userId));
    }
}
