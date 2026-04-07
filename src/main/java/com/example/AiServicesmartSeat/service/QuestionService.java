package com.example.AiServicesmartSeat.service;

import com.example.AiServicesmartSeat.DTO.ExamDropdownDTO;
import com.example.AiServicesmartSeat.DTO.QuestionDTO;
import com.example.AiServicesmartSeat.entity.Notification;
import com.example.AiServicesmartSeat.entity.QuestionEntity;
import com.example.AiServicesmartSeat.entity.QuestionData;
import com.example.AiServicesmartSeat.entity.Timetable;
import com.example.AiServicesmartSeat.repository.NotificationRepository;
import com.example.AiServicesmartSeat.repository.QuestionRepository;
import com.example.AiServicesmartSeat.repository.TimetableRepo;
import com.example.AiServicesmartSeat.util.HelperMethod;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class QuestionService {

    private final ChatClient chatClient;
    private final TimetableRepo timetableRepo;
    private final QuestionRepository questionRepository;
    private final NotificationRepository notificationRepo;
    private final HelperMethod helper;


    //for manual/csv upload question data
    @Transactional
    public void saveAndFinalizePaper(QuestionEntity paper) {
        // Generate a secure 6-character random password
        String generatedPassword = generateExamCode(6);
        paper.setExamPassword(generatedPassword);

        questionRepository.save(paper);
        timetableRepo.markAsGeneratedAndApproved(Long.valueOf(paper.getExamId()));
    }

    //for AI generated question insertion
    @Async
    public void generateQuestions(String contextText, int totalQuestions, Long examId,String userID) {
        try {
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

                //Transform the AI DTOs into a list of nested QuestionData objects
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


                //Create ONE single document for the whole exam
                QuestionEntity examPaper = new QuestionEntity();
                examPaper.setExamId(String.valueOf(examId));
                examPaper.setQuestions(questionList);
                String randomPassword = generateExamCode(6);
                examPaper.setExamPassword(randomPassword); // Set the generated password

                //Save the single document to MongoDB
                questionRepository.save(examPaper);

                timetableRepo.markAsGenerated(examId);

                //for notification to university
                ZonedDateTime istZone = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
                LocalDateTime istLocal = istZone.toLocalDateTime();
                String examName = timetableRepo.getExamNameByTimetable(examId);
                // 1. Use the static builder() method directly
                Notification notification = Notification.builder()
                        .userId(userID)
                        .role("university")
                        .type("QUESTION_GENERATION_DONE")
                        .msg("Questions generation process finished for " + examName)
                        .isRead(false)
                        .createdAt(istLocal)
                        .build();
                // 2. Save the result of the .build() call
                notificationRepo.save(notification);

                System.out.println("✅ Saved single document and password for Exam ID: " + examId + " with " + questionList.size() + " questions.");
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

    private String generateExamCode(int length) {
        // Exclude O, 0, I, 1 to prevent student confusion during the exam
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        java.security.SecureRandom random = new java.security.SecureRandom();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    //fetch exams whose generated questions not approved by university
    public List<ExamDropdownDTO> getExamList() {
        return timetableRepo.findExam();
    }

    @Transactional
    public void approveAndSavePaper(String examId, QuestionEntity updatedData) {
        //fetch Question Paper in MongoDB
        QuestionEntity existingPaper = questionRepository.findByExamId(examId)
                .orElseThrow(() -> new RuntimeException("Question Paper not found in MongoDB for ID: " + examId));

        // Update the fields with the edited data from the frontend
        existingPaper.setQuestions(updatedData.getQuestions());

        questionRepository.save(existingPaper);

        //Update the Approval Status in PostgreSQL Timetable
        Long sqlId = Long.parseLong(examId);
        Timetable timetable = timetableRepo.findById(sqlId)
                .orElseThrow(() -> new RuntimeException("Timetable entry not found in SQL for ID: " + sqlId));

        timetable.setApproved(true);
        timetableRepo.save(timetable);
    }
}