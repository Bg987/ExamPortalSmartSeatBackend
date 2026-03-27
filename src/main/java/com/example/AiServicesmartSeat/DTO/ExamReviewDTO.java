package com.example.AiServicesmartSeat.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ExamReviewDTO {
    private String examId;
    private String enrNumber;
    private int marks;
    private int totalMarks;
    private double percentage;
    private String status;
    private List<QuestionReview> questionReviews;

    @Data
    @AllArgsConstructor
    public static class QuestionReview {
        private String questionText;
        private List<String> options;
        private String studentAnswer;
        private String correctAnswer;
        private boolean isCorrect;
    }
}
