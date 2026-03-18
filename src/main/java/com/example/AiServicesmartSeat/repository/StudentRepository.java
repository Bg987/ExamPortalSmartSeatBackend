package com.example.AiServicesmartSeat.repository;


import com.example.AiServicesmartSeat.entity.Students;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;




@Repository
public interface StudentRepository extends JpaRepository<Students, String> {

    boolean existsByEnrollmentNo(String enrollmentNo);

    @Query("SELECT s.enrollmentNo FROM Students s WHERE s.studentId = :studentId")
    Optional<String> findEnrollmentNoByStudentId(@Param("studentId") Long studentId);

    Optional<Students> findByEnrollmentNo(String enrollmentNo);
}