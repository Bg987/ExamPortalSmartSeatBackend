package com.example.AiServicesmartSeat.controller;

import com.example.AiServicesmartSeat.entity.Timetable;
import com.example.AiServicesmartSeat.repository.QuestionRepository;
import com.example.AiServicesmartSeat.repository.TimetableRepo;
import com.example.AiServicesmartSeat.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.apache.tika.Tika;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import java.util.List;



@RequiredArgsConstructor
@RestController
@RequestMapping("/api/exam")

public class ExamController {

    private final QuestionService questionService;
    private final TimetableRepo timetableRepo;
    private final Tika tika = new Tika();


    @PreAuthorize("hasRole('university')")
    @PostMapping("/generate-from-pdf")
    public ResponseEntity<?> generateFromPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "10") int count,
            @RequestParam Long examId) {

        try {
            // Extract text from PDF
            if(timetableRepo.isQuestionGenerated(examId)){
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

            System.out.println("error in question generation = ");
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('student')")
    @GetMapping("/test")
    public String test(){
        return "protected for studnet";
    }


    @PreAuthorize("hasRole('university')")
    @GetMapping("/ExamWithoutQeustions")
    public ResponseEntity<?> ExamWithoutQeustions(){

        List<Timetable> res = timetableRepo.findByquestionGeneratedFalse();
        if(res.size()==0){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("not found colleges for this exam");
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/health")
    public ResponseEntity<?> health()
    {
        return ResponseEntity.ok("server is done");
    }
}