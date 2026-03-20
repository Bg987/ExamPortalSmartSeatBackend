package com.example.AiServicesmartSeat.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentExamView implements Serializable { // Serializable is required for Caching
    private String examId;
    private String subjectName;
    private Integer duration;
    private LocalTime startTime; // Added Start Time
    private List<StudentQuestion> questions;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StudentQuestion implements Serializable {
        private String text;
        private List<String> options;
    }
}
