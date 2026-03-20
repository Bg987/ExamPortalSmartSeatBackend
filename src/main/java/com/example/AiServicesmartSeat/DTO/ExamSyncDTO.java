package com.example.AiServicesmartSeat.DTO;

import lombok.Data;
import java.util.Map;

@Data
public class ExamSyncDTO {
    private String examId;
    private Map<String, String> answers;
}
