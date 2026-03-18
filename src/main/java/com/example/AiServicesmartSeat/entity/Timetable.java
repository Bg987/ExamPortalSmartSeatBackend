package com.example.AiServicesmartSeat.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Entity
@Table(name = "timeTable")
@NoArgsConstructor
@Data
public class Timetable {

    @Id
    @Column(name="timetable_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subjectid")
    private String subjectId;

    @Column(name = "subject_name", nullable = false, length = 50)
    private String subjectName;

    @Column(name="exam_date",nullable = false)
    private LocalDate examDate;

    @Column(name="start_time", nullable = false)
    private LocalTime startTime = LocalTime.of(9, 0); // Default 09:00 AM

    @Column(name="completed")
    private boolean completed;

    @Column(name="batchid")
    private String batchId;

    @Column(name="branch")
    private String branch;

    @Column(name="semester")
    private Integer semester;

    @Column(name="duration_minutes", nullable = false)
    private Integer durationMinutes = 180; // 3 Hours

    @Column(name = "is_allocated", nullable = false, columnDefinition = "boolean default false")
    private boolean allocated = false;

    @Column(name = "is_question_genrated", nullable = false)
    private boolean questionGenerated = false;

    public boolean isAccessAllowed() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime examStart = LocalDateTime.of(this.examDate, this.startTime);

        // Allowed between (Start - 30 mins) AND (Start + 3 Hours)
        return now.isAfter(examStart.minusMinutes(30)) &&
                now.isBefore(examStart.plusMinutes(durationMinutes));
    }
}
