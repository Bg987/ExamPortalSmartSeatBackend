package com.example.AiServicesmartSeat.repository;


import com.example.AiServicesmartSeat.entity.ResultEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResultRepository extends MongoRepository<ResultEntity, String> {

    // Find all results for a specific exam (Useful for Grade Sheets)
    List<ResultEntity> findByExamId(String examId);

    // Find a specific student's result for a specific exam
    Optional<ResultEntity> findByEnrNumberAndExamId(String enrNumber, String examId);

    // Find all exam results for one student (Student Dashboard)
    List<ResultEntity> findByEnrNumber(String enrNumber);

    // Check if a result already exists to prevent duplicate grading
    boolean existsByEnrNumberAndExamId(String enrNumber, String examId);


    //for analysis

    // Filter results for specific students in a specific exam
    List<ResultEntity> findByExamIdAndEnrNumberIn(String examId, List<String> enrNumbers);
}