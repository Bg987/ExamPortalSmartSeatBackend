package com.example.AiServicesmartSeat.controller;

import com.example.AiServicesmartSeat.DTO.ExamSyncDTO;
import com.example.AiServicesmartSeat.DTO.StudentExamView;
import com.example.AiServicesmartSeat.entity.QuestionEntity;
import com.example.AiServicesmartSeat.entity.Timetable;
import com.example.AiServicesmartSeat.repository.QuestionRepository;
import com.example.AiServicesmartSeat.repository.TimetableRepo;
import com.example.AiServicesmartSeat.service.ExamService;
import com.example.AiServicesmartSeat.service.QuestionService;
import com.example.AiServicesmartSeat.util.HelperMethod;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.apache.tika.Tika;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;



@RequiredArgsConstructor
@RestController
@RequestMapping("/api/exam")

public class ExamController {

    private final QuestionService questionService;
    private final TimetableRepo timetableRepo;
    private final HelperMethod helper;
    private final ExamService examService;
    private final Tika tika = new Tika();


    @PreAuthorize("hasRole('university')")
    @PostMapping("/generate-from-pdf")
    public ResponseEntity<?> generateFromPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "10") int count,
            @RequestParam Long examId) {

        try {

            if(timetableRepo.isQuestionGenerated(examId)){
                throw new BadRequestException("question already generated for this exam");
            }
            // Extract text from PDF
            String extractedText = tika.parseToString(file.getInputStream());
            String currentUserId = helper.getId();
            questionService.generateQuestions(extractedText.substring(0,2000), count,examId,currentUserId);

            // Return simple OK response
            return ResponseEntity.ok(Map.of("status", "ok", "message", "questions generation started in background"));

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


    @PreAuthorize("hasRole('student')")
    @PostMapping("/verify/{examId}")
    public ResponseEntity<?> enterExam(@PathVariable Long examId, @RequestBody Map<String, String> payload) {
        String password = payload.get("password");
        try {

            //ensure access exam before 2 minute of start time
            if(!examService.validateStudent(examId)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error","Access denied. You can only enter between 2 minutes before the start time and the scheduled end of the exam."));
            }

            //check student is actually register for this exam or not
            else if(examService.validateStudent(examId)==null){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error","You are not register for this exam"));
            }

            // This call now validates password EVERY TIME but fetches questions from CACHE
            StudentExamView studentView = examService.verifyAndGetView(examId, password);

            return ResponseEntity.ok(Map.of("sucess", true, "data", studentView));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            //e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/sync")
    public ResponseEntity<?> sync(@RequestBody ExamSyncDTO dto) {
        // 1. Get enrNumber from Session/SecurityContext
        String enrNumber = helper.getEnrNumberIdByUserId();

        // 2. Hand off to the Async Service (Non-blocking)
        examService.processSyncRequest(dto, enrNumber,"IN_PROGRESS");

        // 3. Return immediately (The student sees "Saved" instantly)
        return ResponseEntity.ok().build();
    }

    @PostMapping("/submit")
    public ResponseEntity<Map<String, String>> submit(@RequestBody ExamSyncDTO dto) {
        String enrNumber = helper.getEnrNumberIdByUserId();

        try {
            // Finalize and lock the submission
            examService.processSyncRequest(dto, enrNumber,"COMPLETE");

            Map<String, String> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Exam locked and submitted successfully.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to finalize submission."));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> health()
    {
        return ResponseEntity.ok("server is done");
    }
}