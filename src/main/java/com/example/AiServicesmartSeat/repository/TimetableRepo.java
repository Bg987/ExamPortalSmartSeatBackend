package com.example.AiServicesmartSeat.repository;


import com.example.AiServicesmartSeat.entity.Timetable;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TimetableRepo extends JpaRepository<Timetable, Long> {

    @Modifying
    @Transactional
    // We use the Java field name 'questionGenerated' here
    @Query("UPDATE Timetable t SET t.questionGenerated = true WHERE t.id = :id")
    int markAsGenerated(@Param("id") Long id);

    @Query("SELECT t.questionGenerated FROM Timetable t WHERE t.id = :id")
    Boolean isQuestionGenerated(@Param("id") Long id);

    List<Timetable> findByquestionGeneratedFalse();
}