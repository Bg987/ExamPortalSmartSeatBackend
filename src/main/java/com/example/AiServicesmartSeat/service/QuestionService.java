package com.example.AiServicesmartSeat.service;

import com.example.AiServicesmartSeat.DTO.QuestionDTO;
import com.example.AiServicesmartSeat.entity.QuestionEntity;
import com.example.AiServicesmartSeat.entity.QuestionData;
import com.example.AiServicesmartSeat.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class QuestionService {

    private final ChatClient chatClient;
    private final QuestionRepository questionRepository;

    @Async
    public void generateQuestions(String contextText, int totalQuestions, String examId) {
        try {
            if(questionRepository.existsByExamId(examId)){
                throw new RuntimeException("question already generated for this exam");
            }
            String limitedContext = truncateContext(contextText, 2000);

            String prompt = """
                    # ROLE
                    You are an Expert Academic Examiner creating high-quality, technically accurate MCQs.
                    
                    # CONTEXT
                    {context}
                    
                    # TASK
                    Generate {count} MCQs with 4 options and 1 correct index.
            
                    # OUTPUT FORMAT
                    Return ONLY a JSON array:
                    [
                      {{
                        "text": "Question?",
                        "options": ["A", "B", "C", "D"],
                        "correctAnswerIndex": 0
                      }}
                    ]
                    """;

            List<QuestionDTO> questions = this.chatClient.prompt()
                    .user(u -> u.text(prompt)
                            .param("context", limitedContext)
                            .param("count", totalQuestions))
                    .call()
                    .entity(new ParameterizedTypeReference<List<QuestionDTO>>() {});

            if (questions != null && !questions.isEmpty()) {

                // 1. Transform the AI DTOs into a list of nested QuestionData objects
                List<QuestionData> questionList = questions.stream()
                        .map(dto -> {
                            randomizeOptions(dto); // Randomize before mapping
                            return new QuestionData(
                                    dto.getText(),
                                    dto.getOptions(),
                                    dto.getCorrectAnswerIndex()
                            );
                        })
                        .collect(Collectors.toList());

                // 2. Create ONE single document for the whole exam
                QuestionEntity examPaper = new QuestionEntity();
                examPaper.setExamId(examId);
                examPaper.setQuestions(questionList);

                // 3. Save the single document to MongoDB
                questionRepository.save(examPaper);

                System.out.println("✅ Saved single document for Exam ID: " + examId + " with " + questionList.size() + " questions.");
            } else {
                System.out.println(">>> AI returned an empty list.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void randomizeOptions(QuestionDTO question) {
        String correctText = question.getOptions().get(question.getCorrectAnswerIndex());
        Collections.shuffle(question.getOptions());
        question.setCorrectAnswerIndex(question.getOptions().indexOf(correctText));
    }

    private String truncateContext(String text, int maxLength) {
        return (text == null || text.length() <= maxLength) ? text : text.substring(0, maxLength);
    }
}