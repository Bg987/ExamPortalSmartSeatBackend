package com.example.AiServicesmartSeat.repository;


import com.example.AiServicesmartSeat.entity.ExamAnalytics;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ExamAnalyticsRepository extends MongoRepository<ExamAnalytics, String> {

    // Find analytics by the composite ID (examId_collegeId)
    Optional<ExamAnalytics> findById(String id);

    // Find all college reports for a single exam
    Optional<ExamAnalytics> findByExamIdAndCollegeId(String examId, Long collegeId);
}