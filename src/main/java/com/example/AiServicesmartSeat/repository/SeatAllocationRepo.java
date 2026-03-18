package com.example.AiServicesmartSeat.repository;

import com.example.AiServicesmartSeat.entity.SeatAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Map;

@Repository
public interface SeatAllocationRepo extends JpaRepository<SeatAllocation, Long> {

    //fetch exam details for stundent complete or not based on flag
    @Query("SELECT DISTINCT s.timetable.id as id, " +" s.timetable.startTime as time, "+"s.timetable.durationMinutes as duration ,"+
            "CONCAT(s.timetable.branch, ' - Sem ', s.timetable.semester, ' - ', s.timetable.subjectId, ' - ', s.timetable.examDate) as examName, " +
            "s.timetable.examDate as Date " + // Removed trailing comma, ensured space before FROM
            "FROM SeatAllocation s " +
            "WHERE s.student.enrollmentNo = :enrollmentNo " +
            "AND s.timetable.completed = :status")
    List<Map<String, Object>> findAllocatedExamsByStatus(
            @Param("enrollmentNo") String enrollmentNo,
            @Param("status") boolean status);
}