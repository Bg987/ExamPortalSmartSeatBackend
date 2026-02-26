package com.example.AiServicesmartSeat.util;


import com.example.AiServicesmartSeat.entity.StudentEmbedding;
import com.example.AiServicesmartSeat.repository.StudentEmbeddingRepository;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@Component
@AllArgsConstructor
public class BiometricUtility {

    private final StudentEmbeddingRepository stuEmbeddingRepo;
    private final String FAST_API_URL = "https://nonswimming-nonseriously-lester.ngrok-free.dev/verify";


    public float[] embeddingsFromEnrNumber(String enrNumber){
        StudentEmbedding studentData = stuEmbeddingRepo.findByEnrollmentNo(enrNumber)
                .orElse(null);

        if (studentData == null) {
            return null;
        }

        return studentData.getEmbeddingAsArray(); // Ensure this matches your Entity getter
    }

    public ResponseEntity<Map> pythonApi(MultipartFile image,float[] storedEmbedding) throws Exception {

        // 3. Prepare Multipart Request for Python
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // Wrap the image bytes so RestTemplate can send it as a file
        ByteArrayResource imageResource = new ByteArrayResource(image.getBytes()) {
            @Override
            public String getFilename() { return "capture.jpg"; }
        };

        body.add("file", imageResource);
        body.add("embedding_json", Arrays.toString(storedEmbedding));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        // 4. Exchange data with Python AI Service
        ResponseEntity<Map> response = restTemplate.postForEntity(FAST_API_URL, requestEntity, Map.class);

        if (response.getBody() == null) {
            throw new Exception("Empty response from AI service");
        }
        return response;
    }

}
