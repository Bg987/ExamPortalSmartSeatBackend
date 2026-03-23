package com.example.AiServicesmartSeat.repository;

import com.example.AiServicesmartSeat.entity.SeatAllocation;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    boolean existsByStudent_EnrollmentNoAndTimetable_Id(String enrollmentNo, Long timetableId);

    Optional<SeatAllocation> findByStudent_EnrollmentNoAndTimetable_Id(String enr, Long timetableId);

    @Modifying
    @Transactional
    @Query("UPDATE SeatAllocation s SET s.attendance = true WHERE s.student.enrollmentNo = :enr AND s.timetable.id = :tId")
    void markAsAttended(String enr, Long tId);


    //as exam submit set allocation table too
    @Transactional
    @Modifying
    @Query("UPDATE SeatAllocation s SET s.isSubmitted = true " +
            "WHERE s.student.enrollmentNo = :enrNumber " +
            "AND s.timetable.id = :timetableId")
    int markAsSubmitted(@Param("enrNumber") String enrNumber,
                        @Param("timetableId") Long timetableId);
}