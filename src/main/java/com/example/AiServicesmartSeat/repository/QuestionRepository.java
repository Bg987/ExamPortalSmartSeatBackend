package com.example.AiServicesmartSeat.repository;

import com.example.AiServicesmartSeat.entity.QuestionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionRepository extends MongoRepository<QuestionEntity, String> {

    /**
     * Custom query method: Spring Data will automatically implement this
     * to find all questions matching a specific examId.
     */
    List<QuestionEntity> findByExamId(String examId);
    Boolean existsByExamId(String examId);
    /**
     * Deletes all questions for a specific examId.
     * Useful if you want to "regenerate" questions for an exam.
     */
    void deleteByExamId(String examId);

}