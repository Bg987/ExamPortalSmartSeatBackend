package com.example.AiServicesmartSeat.DTO;

import java.util.List;

public class QuestionDTO {
    private String text;
    private List<String> options;
    private int correctAnswerIndex;

    // Standard Getters and Setters
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public int getCorrectAnswerIndex() { return correctAnswerIndex; }
    public void setCorrectAnswerIndex(int correctAnswerIndex) { this.correctAnswerIndex = correctAnswerIndex; }
}