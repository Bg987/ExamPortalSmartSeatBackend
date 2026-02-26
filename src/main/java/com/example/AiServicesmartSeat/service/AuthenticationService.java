package com.example.AiServicesmartSeat.service;

import com.example.AiServicesmartSeat.DTO.ApiResponse;
import com.example.AiServicesmartSeat.entity.StudentEmbedding;
import com.example.AiServicesmartSeat.repository.StudentEmbeddingRepository;
import com.example.AiServicesmartSeat.repository.StudentRepository;
import com.example.AiServicesmartSeat.util.BiometricUtility;
import com.example.AiServicesmartSeat.util.CookieUtil;
import com.example.AiServicesmartSeat.util.HelperMethod;
import com.example.AiServicesmartSeat.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final StudentRepository stuRepo;
    private final CookieUtil cookieU;
    private final BiometricUtility BiometricUtil;
    private final HelperMethod helper;


    public ResponseEntity<ApiResponse> faceAuth(String enrNumber, MultipartFile image, HttpServletResponse res) {
        try {
            //Check if Student exists in Primary Table
            if (!stuRepo.existsByEnrollmentNo(enrNumber)) {
                return helper.buildResponse(HttpStatus.NOT_FOUND, "error", "Enrollment number not found", false, null);
            }

            //fetch embeddings if student found in database
            float[] storedEmbedding = BiometricUtil.embeddingsFromEnrNumber(enrNumber);
            if(storedEmbedding==null){
                return helper.buildResponse(HttpStatus.BAD_REQUEST, "error", "No face data registered for this student", false, null);
            }

            //image verification
            ResponseEntity<Map> response = BiometricUtil.pythonApi(image,storedEmbedding);
            Boolean isVerified = (Boolean) response.getBody().get("verified");
            Double distance = (Double) response.getBody().get("distance");

            // res. handle from python api
            if (Boolean.TRUE.equals(isVerified)) {

                jakarta.servlet.http.Cookie cookie = cookieU.setCookie(enrNumber);
                res.addCookie(cookie);
                return helper.buildResponse(HttpStatus.OK, "success", "Identity Verified", true, distance);
            } else {
                return helper.buildResponse(HttpStatus.UNAUTHORIZED, "fail", "Face mismatch detected", false, distance);
            }

        } catch (IOException e) {
            return helper.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "error", "Image processing failed", false, null);
        } catch (Exception e) {
            return helper.buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "error", "AI Service Error: " + e.getMessage(), false, null);
        }
    }

    public ResponseEntity<String> logout(HttpServletResponse response){

        jakarta.servlet.http.Cookie cookie= cookieU.delCookie("AUTH_TOKEN");
        response.addCookie(cookie);
        return ResponseEntity.status(200).body("logout successfully");
    }

    // Helper method to keep code clean and DRY (Don't Repeat Yourself)

}