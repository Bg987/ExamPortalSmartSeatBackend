package com.example.AiServicesmartSeat.util;

import com.example.AiServicesmartSeat.DTO.ApiResponse;
import com.example.AiServicesmartSeat.entity.College;
import com.example.AiServicesmartSeat.repository.CollegeRepository;
import com.example.AiServicesmartSeat.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import static io.jsonwebtoken.JwsHeader.ALGORITHM;


@Component
@RequiredArgsConstructor
public class HelperMethod {

    private final StudentRepository stuRepo;
    private final CollegeRepository collegeRepo;
    private static final String ALGORITHM = "AES";

    @Value("${app.security.aes-seed}")
    private String aesKeySeed;


    private SecretKeySpec getSecretKey() throws Exception {
        byte[] key = aesKeySeed.getBytes(StandardCharsets.UTF_8);
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        key = sha.digest(key); // This results in exactly 32 bytes
        return new SecretKeySpec(key, ALGORITHM);
    }

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

    public String getEnrNumberIdByUserId() throws Exception {
        String studentId = null;
        try {
            studentId = getId();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println(studentId);
        String finalStudentId = studentId;
        return stuRepo.findEnrollmentNoByStudentId(Long.parseLong(studentId)) // Or the method we fixed earlier
                .orElseThrow(() -> new RuntimeException("enr. number not found for student ID: " + finalStudentId));
    }

    public String getId() throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String principal = auth.getPrincipal().toString();
        return String.valueOf(decrypt(principal));
    }

    public Long getCollegeIdByUserId() throws Exception {
        String userId= getId();
        return collegeRepo.findByUser_userId(Long.parseLong(userId)) // Or the method we fixed earlier
                .map(College::getCollegeId)
                .orElseThrow(() -> new RuntimeException("College not found for User ID: " + userId));
    }

    public String encrypt(Long id) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
        byte[] encryptedBytes = cipher.doFinal(String.valueOf(id).getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public Long decrypt(String encryptedId) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedId);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return Long.parseLong(new String(decryptedBytes));
    }
}
