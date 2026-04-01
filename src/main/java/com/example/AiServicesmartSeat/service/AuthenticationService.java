package com.example.AiServicesmartSeat.service;

import com.example.AiServicesmartSeat.DTO.ApiResponse;
import com.example.AiServicesmartSeat.entity.BlockSession;
import com.example.AiServicesmartSeat.entity.StudentEmbedding;
import com.example.AiServicesmartSeat.entity.Students;
import com.example.AiServicesmartSeat.repository.BlockSessionRepository;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final StudentRepository stuRepo;
    private final BlockSessionRepository blockSessionRepository;
    private final CookieUtil cookieU;
    private final BiometricUtility BiometricUtil;
    private final HelperMethod helper;
    private final JwtUtil jwtUtil;
    private final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");//for deployment

    public ResponseEntity<ApiResponse> faceAuth(String enrNumber, MultipartFile image, HttpServletResponse res) {
        try {
            //Check if Student exists in Primary Table
            if (!stuRepo.existsByEnrollmentNo(enrNumber)) {
                return helper.buildResponse(HttpStatus.NOT_FOUND, "error", "Enrollment number not found", false, null,null);
            }

            //fetch embeddings if student found in database
            float[] storedEmbedding = BiometricUtil.embeddingsFromEnrNumber(enrNumber);
            if(storedEmbedding==null){
                return helper.buildResponse(HttpStatus.BAD_REQUEST, "error", "No face data registered for this student", false, null,null);
            }

            //image verification
            ResponseEntity<Map> response = BiometricUtil.pythonApi(image,storedEmbedding);
            Boolean isVerified = (Boolean) response.getBody().get("verified");
            Double distance = (Double) response.getBody().get("distance");

            // res. handle from python api
            if (Boolean.TRUE.equals(isVerified)) {
                Students student = stuRepo
                        .findByEnrollmentNo(enrNumber)
                        .orElseThrow(() -> new RuntimeException("Student not found"));
                Long studentId = student.getStudentId();
                jakarta.servlet.http.Cookie cookie = cookieU.setCookie(studentId,"student");
                res.addCookie(cookie);

                //for compiler backend authentication
                String token2 = jwtUtil.generatToken2(student.getEmail(),"STUDENT");

                return helper.buildResponse(HttpStatus.OK, "success", "Identity Verified", true, distance,token2);
            } else {
                jakarta.servlet.http.Cookie cookie= cookieU.delCookie("AUTH_JWT");
                res.addCookie(cookie);
                return helper.buildResponse(HttpStatus.UNAUTHORIZED, "fail", "Face mismatch detected", false, distance,null);
            }

        } catch (IOException e) {
            jakarta.servlet.http.Cookie cookie= cookieU.delCookie("AUTH_JWT");
            res.addCookie(cookie);
            return helper.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "error", "Image processing failed", false, null,null);
        } catch (Exception e) {
            jakarta.servlet.http.Cookie cookie= cookieU.delCookie("AUTH_JWT");
            res.addCookie(cookie);
            return helper.buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "error", "AI Service Error: " + e.getMessage(), false, null,null);
        }
    }

    public ResponseEntity<String> logout(HttpServletResponse response){

        jakarta.servlet.http.Cookie cookie= cookieU.delCookie("AUTH_JWT");
        response.addCookie(cookie);
        return ResponseEntity.status(200).body("logout successfully");
    }

    //procudure to check whether student block or not before attempt face login
    public String checkBlockStatus(String enrollmentNumber) {
        Optional<BlockSession> blockEntry = blockSessionRepository.findByEnrNumber(enrollmentNumber);

        if (blockEntry.isPresent()) {
            LocalDateTime nowIST = ZonedDateTime.now(IST_ZONE).toLocalDateTime();
            LocalDateTime blockedAt = blockEntry.get().getBlockedAt();

            long minutesPassed = Duration.between(blockedAt, nowIST).toMinutes();

            if (minutesPassed < 45) {
                // Calculate and format the unlock time
                LocalDateTime unlockTime = blockedAt.plusMinutes(45);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");

                return "Your account has been temporarily locked due to a proctoring violation. A mandatory 45-minute cooldown is in effect. You will be eligible to log in at " + unlockTime.format(formatter) + " (IST).kindly close this tab";
            } else {
                // Penalty time is over, remove from DB
                blockSessionRepository.deleteByEnrNumber(enrollmentNumber);
            }
        }
        return null; // Not blocked
    }

}