package com.example.AiServicesmartSeat.repository;

import com.example.AiServicesmartSeat.entity.StudentEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentEmbeddingRepository extends JpaRepository<StudentEmbedding, Long> {

    /**
     * Finds the face embedding array by joining with the Students table
     * based on the Enrollment Number.
     */
    @Query("SELECT e FROM StudentEmbedding e " +
            "JOIN Students s ON e.studentId = s.studentId " +
            "WHERE s.enrollmentNo = :enrollmentNo")
    Optional<StudentEmbedding> findByEnrollmentNo(@Param("enrollmentNo") String enrollmentNo);
}