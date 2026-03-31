package com.example.AiServicesmartSeat.entity;


import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;
import java.util.Map;

@Document(collection = "exam_college_analytics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamAnalytics {

    @Id
    private String id; // Unique combination: examId_collegeId (e.g., "91_15")

    // --- Identification ---
    private String examId;      // From Timetable SQL
    private String subjectCode; // From Timetable SQL
    private Long collegeId;     // From College SQL
    private String collegeName; // Cached for fast dashboard rendering

    // --- Participation Stats (From SQL: seat_allocation) ---
    private int totalRegistered;  // Total students allocated
    private int totalPresent;     // attendance = true
    private int totalAbsent;      // attendance = false
    private int totalSubmitted;   // isSubmitted = true
    private double attendancePercentage;

    // --- Performance Stats (From MongoDB: results) ---
    private int totalPassed;      // status = "PASSED"
    private int totalFailed;      // status = "FAILED"
    private double passPercentage;
    private double averageMarks;
    private int highestMarks;

    // --- Distribution (For Bar/Pie Charts) ---
    // Example: {"Distinction": 20, "First Class": 45, "Pass": 30, "Fail": 15}
    private Map<String, Integer> gradeBreakdown;

    private Date lastUpdated;    // When was this calculation last synced?
}
