package com.example.AiServicesmartSeat.service;


import com.example.AiServicesmartSeat.entity.QuestionEntity;
import com.example.AiServicesmartSeat.repository.QuestionRepository;
import com.example.AiServicesmartSeat.repository.TimetableRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;

@RequiredArgsConstructor
@Service
public class CollegeService {

    private final TimetableRepo timetableRepo;
    private final QuestionRepository questionRepository;

    public List<Map<String, Object>> getUpcomingExamsWithPasswords(Long collegeId) {
        LocalTime now = LocalTime.now();
        LocalTime limit = now.plusMinutes(30);

        List<Map<String, Object>> sqlResults = timetableRepo.findExamsWithin15MinWindow(collegeId, limit);

        if (sqlResults.isEmpty()) {
            return Collections.emptyList();
        }

        // Create a new list to hold our modified, mutable maps
        List<Map<String, Object>> mutableResults = new ArrayList<>();

        for (Map<String, Object> readOnlyExam : sqlResults) {
            // 1. Convert the read-only TupleBackedMap to a mutable HashMap
            Map<String, Object> exam = new HashMap<>(readOnlyExam);

            String examId = String.valueOf(exam.get("id"));

            // 2. Fetch from MongoDB
            Optional<QuestionEntity> mongoDataOptional = questionRepository.findByExamId(examId);

            if (mongoDataOptional.isPresent()) {
                QuestionEntity mongoData = mongoDataOptional.get();
                String password = mongoData.getExamPassword();

                if (password != null && !password.trim().isEmpty()) {
                    exam.put("examPassword", password);
                } else {
                    exam.put("examPassword", "NOT_GENERATED");
                }
            } else {
                exam.put("examPassword", "NOT_GENERATED");
            }

            // Add the new mutable map to our list
            mutableResults.add(exam);
        }

        return mutableResults;
    }
}
