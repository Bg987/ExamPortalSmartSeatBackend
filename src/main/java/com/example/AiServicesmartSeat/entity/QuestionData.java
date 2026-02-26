package com.example.AiServicesmartSeat.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionData {
    private String text;
    private List<String> options;
    private int correctAnswerIndex;
}