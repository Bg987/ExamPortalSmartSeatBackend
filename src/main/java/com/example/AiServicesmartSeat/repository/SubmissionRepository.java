package com.example.AiServicesmartSeat.repository;



import com.example.AiServicesmartSeat.entity.StudentSubmission;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends MongoRepository<StudentSubmission, String> {



    Optional<StudentSubmission> findByEnrNumberAndExamId(String enrNumber, String examId);

    List<StudentSubmission> findByExamId(String examId);


    // Custom query to find only "SUBMITTED" students for grading
    @Query("{ 'examId': ?0, 'status': 'SUBMITTED' }")
    List<StudentSubmission> findSubmittedSubmissionsByExamId(String examId);
}
