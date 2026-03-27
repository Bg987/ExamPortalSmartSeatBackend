package com.example.AiServicesmartSeat.repository;


import com.example.AiServicesmartSeat.entity.Timetable;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface TimetableRepo extends JpaRepository<Timetable, Long> {

    @Modifying
    @Transactional
    // We use the Java field name 'questionGenerated' here
    @Query("UPDATE Timetable t SET t.questionGenerated = true WHERE t.id = :id")
    int markAsGenerated(@Param("id") Long id);

    @Query("SELECT t.questionGenerated FROM Timetable t WHERE t.id = :id")
    Boolean isQuestionGenerated(@Param("id") Long id);

    List<Timetable> findByquestionGeneratedFalse();

        @Query("""
    SELECT DISTINCT t.id as id, 
           CONCAT(t.branch, ' - Sem ', t.semester, ' - ', t.subjectId, ' - ', t.examDate) as examName,
           t.startTime as startTime,
           t.examDate as examDate
    FROM SeatAllocation s 
    JOIN s.timetable t
    WHERE s.college.id = :collegeId 
      AND t.allocated = true 
      AND t.completed = false
      AND t.examDate = CURRENT_DATE
      AND t.startTime <=:limit
    """)
        List<Map<String, Object>> findExamsWithin15MinWindow(
                @Param("collegeId") Long collegeId,
                @Param("limit") java.time.LocalTime limit
        );

    @Query("""
        SELECT CONCAT(t.branch, ' - Sem ', t.semester, 
              ' - ', t.subjectId, 
              ' - ', t.examDate)
           FROM Timetable t
        WHERE t.id = :timeTableId""")
    String getExamNameByTimetable(@Param("timeTableId") Long timeTableId);

    @Transactional
    @Modifying
    @Query("UPDATE Timetable t SET t.completed = true WHERE t.id = :examId")
    int markExamAsCompleted(@Param("examId") Long examId);

    boolean existsByIdAndCompletedTrue(Long examId);
}
