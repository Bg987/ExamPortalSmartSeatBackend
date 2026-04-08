package com.example.AiServicesmartSeat.controller;


import com.example.AiServicesmartSeat.DTO.ExamDropdownDTO;
import com.example.AiServicesmartSeat.entity.ExamAnalytics;
import com.example.AiServicesmartSeat.repository.ExamAnalyticsRepository;
import com.example.AiServicesmartSeat.repository.TimetableRepo;
import com.example.AiServicesmartSeat.service.AnalyticsService;
import com.example.AiServicesmartSeat.service.QuestionService;
import com.example.AiServicesmartSeat.util.HelperMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/exam")
public class AnalyticsController {

    private final ExamAnalyticsRepository analyticsRepo;
    private final AnalyticsService analyticsService;
    private final HelperMethod helper;
    private final QuestionService questionService;


    //fetch exams whose question approval not done
    @PreAuthorize("hasRole('university')")
    @GetMapping("/getExam")
    public ResponseEntity<List<ExamDropdownDTO>> getExam() {
        List<ExamDropdownDTO> exams = questionService.getExamList();

        if (exams.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(exams);
    }


    @PreAuthorize("hasRole('university')")
    @GetMapping("/completed")
    public ResponseEntity<List<ExamDropdownDTO>> getCompletedExams() {
        List<ExamDropdownDTO> exams = analyticsService.getCompletedExamsList();

        if (exams.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(exams);
    }

    @PreAuthorize("hasRole('university')")
    @PostMapping("/getAnalyticsUniversity/{examId}/{collegeId}")
    public ResponseEntity<?> getAnalyticsUniversity(@PathVariable Long examId,@PathVariable Long collegeId) {

        return analyticsRepo.findByExamIdAndCollegeId(String.valueOf(examId), collegeId)
                .map(report -> ResponseEntity.ok(report))   // If present, return 200 OK
                .orElseGet(() -> ResponseEntity.notFound().build());    // If empty, return 404 Not Found
    }

    //fetch analysis details based on examId and
    @PreAuthorize("hasAnyRole('university', 'college')")
    @PostMapping(value = {"/getAnalyticsCollege/{examId}", "/getAnalyticsCollege/{examId}/{collegeID}"})
    public ResponseEntity<?> getAnalyticsCollege(
            @PathVariable Long examId,
            @PathVariable(required = false) Long collegeID) {

        Long finalCollegeId;

        // 1. Logic: If collegeID is passed in URL, it's a University request.
        // If collegeID is NOT passed, it's a College request (pull from Security Context).
        if (collegeID != null) {
            // Guard: Prevent a user with ONLY 'college' role from passing an ID in the URL
            finalCollegeId = collegeID;
        } else {

            finalCollegeId = helper.getCollegeIdByUserId();
        }

        String compositeId = examId + "_" + finalCollegeId;

        return analyticsRepo.findById(compositeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}