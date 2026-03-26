package com.example.AiServicesmartSeat.controller;


import com.example.AiServicesmartSeat.repository.TimetableRepo;
import com.example.AiServicesmartSeat.service.CollegeService;
import com.example.AiServicesmartSeat.util.HelperMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/exam")
public class CollegeController {

    private final HelperMethod helper;
    private final CollegeService collegeService;

    @PreAuthorize("hasRole('college')")
    @GetMapping("/getExamPassword")
    public ResponseEntity<?> getExamPassword() {

        Long collegeId = helper.getCollegeIdByUserId();
        List<Map<String, Object>> data = collegeService.getUpcomingExamsWithPasswords(collegeId);

        if (data.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "status", "EMPTY",
                    "message", "No exams currently in college within the 30-minute authorization window."
            ));
        }

        return ResponseEntity.ok(data);
    }

    //for telegram chatbot this will be called inside server 1
    @GetMapping("/getExamPasswordOpen/{collegeId}")
    public ResponseEntity<?> getExamPasswordOpen(@PathVariable("collegeId") Long collegeId) {

        List<Map<String, Object>> data = collegeService.getUpcomingExamsWithPasswords(collegeId);

        if (data.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "status", "EMPTY",
                    "message", "No exams currently in college within the 30-minute authorization window."
            ));
        }

        return ResponseEntity.ok(data);
    }
}
