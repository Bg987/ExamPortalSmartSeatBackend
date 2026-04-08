package com.example.AiServicesmartSeat.controller;

import com.example.AiServicesmartSeat.service.StudentService;
import com.example.AiServicesmartSeat.util.HelperMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ExamStudent")
public class studentExamController {

    private final StudentService stuService;
    private final HelperMethod helper;




    @PreAuthorize("hasRole('student')")
    @GetMapping("/getStudentIncomplteExam")
    public ResponseEntity<?> getStudentIncomplteExam(){


        String enrNumber = stuService.getEnrNumber();

        //fetch exams which is incomplete
        List<Map<String, Object>> examNameAndId= stuService.getExamList(enrNumber,false);
        if (examNameAndId.isEmpty()) {
            return ResponseEntity
                    .noContent() // Sets status to 204
                    .build();    // No body allowed
        }

        Map<String, Object> response = new HashMap<>();
        response.put("enrNumber", enrNumber);
        response.put("exams", examNameAndId);
        return ResponseEntity.ok(response);
    }
}
