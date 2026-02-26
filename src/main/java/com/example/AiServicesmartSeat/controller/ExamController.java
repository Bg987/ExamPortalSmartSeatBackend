package com.example.AiServicesmartSeat.controller;

import com.example.AiServicesmartSeat.repository.QuestionRepository;
import com.example.AiServicesmartSeat.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.apache.tika.Tika;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exam")
@CrossOrigin(origins = "*")
public class ExamController {

    private final QuestionService questionService;
    private final Tika tika = new Tika();
    private final QuestionRepository qrepo;

    @GetMapping("/x")
    public ResponseEntity<?> x() {
        // This was set by the filter above!
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Check if the user is actually authenticated
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired or invalid");
        }

        // The 'Principal' is the enrollment number we set in the filter
        String enrollmentNo = (String) auth.getPrincipal();

        // Fetch questions only for this student
        return ResponseEntity.ok("Welcome " + enrollmentNo + ", here are your questions...");
    }


    @GetMapping("/check")
    public Boolean check(){
        return qrepo.existsByExamId("123");
    }


    @PostMapping("/generate-from-pdf")
    public ResponseEntity<?> generateFromPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "10") int count) {
        String examId = "123";
        try {
            // Extract text from PDF
            if(qrepo.existsByExamId(examId)){
                throw new BadRequestException("question already generated for this exam");
            }
            String extractedText = tika.parseToString(file.getInputStream());

            // This service method already has System.out.println logic
            // to print questions to the console
            questionService.generateQuestions(extractedText.substring(0,2000), count,examId);

            // Return simple OK response
            return ResponseEntity.ok(Map.of("status", "ok", "message", "questions generate in background"));

        }
        catch(BadRequestException e){
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}