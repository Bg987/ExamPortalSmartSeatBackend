package com.example.AiServicesmartSeat.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "student_submissions")
// Compound index ensures we can quickly find a specific student's exam record
@CompoundIndex(name = "student_exam_idx", def = "{'enrNumber': 1, 'examId': 1}", unique = true)
public class StudentSubmission {

    @Id
    private String id;

    private String enrNumber; // Enrollment Number from LocalStorage/Session

    private String examId;    // The ID of the exam

    private String status;    // "IN_PROGRESS" or "SUBMITTED"

    private LocalDateTime lastSynced;

    /**
     * The Key is the Question Text
     * The Value is the Selected Option Text
     */
    private Map<String, String> answers;
}
