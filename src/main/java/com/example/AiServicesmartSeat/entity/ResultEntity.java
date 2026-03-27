package com.example.AiServicesmartSeat.entity;


import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultEntity {


    @Id
    private String id;
    private String enrNumber;  // "2215IT016"
    private String examId;     // "91"
    private int marks;         // Obtained Marks
    private int totalMarks;    // Total Questions
    private double percentage;
    private String status;     // "PASSED" or "FAILED"
}
