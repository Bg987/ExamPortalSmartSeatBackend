package com.example.AiServicesmartSeat.repository;



import com.example.AiServicesmartSeat.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface SubjectRepository  extends JpaRepository<Subject, String> {
    List<Subject> findBySemester(Integer semester);

    /**
     * Finds subjects for a specific branch and semester.
     * Useful for branch-specific filtering in the UI.
     */
    List<Subject> findByBranchAndSemester(String branch, Integer semester);

    /**
     * Check if a subject exists for a specific branch and semester.
     * Used in your validation logic during timetable creation.
     */
    boolean existsBySubjectIdAndBranchAndSemester(String subjectId, String branch, Integer semester);
}
