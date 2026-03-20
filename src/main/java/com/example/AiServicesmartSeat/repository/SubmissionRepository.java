package com.example.AiServicesmartSeat.repository;



import com.example.AiServicesmartSeat.entity.StudentSubmission;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface SubmissionRepository extends MongoRepository<StudentSubmission, String> {
    Optional<StudentSubmission> findByEnrNumberAndExamId(String enrNumber, String examId);
}
