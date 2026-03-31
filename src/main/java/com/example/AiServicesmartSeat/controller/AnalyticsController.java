package com.example.AiServicesmartSeat.controller;


import com.example.AiServicesmartSeat.DTO.ExamDropdownDTO;
import com.example.AiServicesmartSeat.entity.ExamAnalytics;
import com.example.AiServicesmartSeat.repository.ExamAnalyticsRepository;
import com.example.AiServicesmartSeat.repository.TimetableRepo;
import com.example.AiServicesmartSeat.service.AnalyticsService;
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

    @PreAuthorize("hasRole('university')")
    @PostMapping("/getAnalyticsCollege/{examId}/{collegeID}")
    public ResponseEntity<?> getAnalyticsCollege(@PathVariable Long examId,@PathVariable Long collegeID) {
        String compositeId = examId + "_" + collegeID;
        return analyticsRepo.findById(compositeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}