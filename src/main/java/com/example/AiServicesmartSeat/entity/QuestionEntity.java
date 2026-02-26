package com.example.AiServicesmartSeat.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "exam_papers") // Renamed to reflect it's a full paper
public class QuestionEntity {

    @Id
    private String id;

    @Indexed(unique = true) // Now examId IS unique because it's one doc per exam
    private String examId;

    private List<QuestionData> questions;

    public  QuestionEntity(String examId, List<QuestionData> questions) {
        this.examId = examId;
        this.questions = questions;
    }
}