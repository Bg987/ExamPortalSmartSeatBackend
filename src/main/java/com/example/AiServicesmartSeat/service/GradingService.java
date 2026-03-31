package com.example.AiServicesmartSeat.service;

import com.example.AiServicesmartSeat.DTO.ExamReviewDTO;
import com.example.AiServicesmartSeat.entity.*;
import com.example.AiServicesmartSeat.repository.*;
import com.example.AiServicesmartSeat.entity.QuestionEntity; // Add this if it's a separate file
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class GradingService {

    private final ResultRepository resultRepository;
    private final NotificationRepository notificationRepository;
    private final SubmissionRepository submissionRepository;
    private final StudentRepository studentRepository;
    private final TimetableRepo timetableRepo;
    private final QuestionRepository questionRepository;
    private final AnalyticsService analyticsService;

    public CompletableFuture<String> gradeEntireExamAsync(QuestionEntity exam,String id) {

        String examId = exam.getExamId();
        timetableRepo.markExamAsCompleted(Long.valueOf(examId));


        String examName= timetableRepo.getExamNameByTimetable(Long.valueOf(examId));
        Long totalMark = (long) exam.getQuestions().size();
        return CompletableFuture.supplyAsync(() -> {
            // 1. Fetch Submissions from MongoDB
            List<StudentSubmission> submissions = submissionRepository.findByExamId(examId);
            if (submissions.isEmpty()) return "No submissions found.";

            // 2. Fetch Student Details from MySQL to get College IDs
            List<String> enrNumbers = submissions.stream()
                    .map(StudentSubmission::getEnrNumber)
                    .toList();

            List<Students> studentDetails = studentRepository.findAllByEnrollmentNoIn(enrNumbers);

            // Create a Map for quick lookup: EnrollmentNo -> CollegeID
            Map<String, String> enrToCollegeMap = studentDetails.stream()
                    .collect(Collectors.toMap(
                            Students::getEnrollmentNo,
                            s -> String.valueOf(s.getCollegeId())
                    ));

            // 3. Calculate Results & Save (MongoDB)
            List<ResultEntity> resultsToSave = submissions.stream()
                    .map(sub -> calculateSingleResult(exam, sub))
                    .toList();
            resultRepository.saveAll(resultsToSave);

            // 4. GENERATE NOTIFICATIONS (JPA/MySQL)
            List<Notification> notifications = new java.util.ArrayList<>();

            // A. Student Notifications
            resultsToSave.forEach(res -> {
                notifications.add(Notification.builder()
                        .userId(res.getEnrNumber())
                        .role("STUDENT")
                        .type("RESULT_OUT")
                        .msg("Result for Exam " + examName + " is out. Score: " + res.getMarks()+" out of "+totalMark)
                        .isRead(false)
                        .build());
            });

            // B. College Notifications (Using the Map we created)
            enrToCollegeMap.values().stream()
                    .distinct()
                    .forEach(collegeId -> {
                        notifications.add(Notification.builder()
                                .userId(collegeId)
                                .role("COLLEGE")
                                .type("RESULT_OUT")
                                .msg("Results for Exam " + examName + " have been published for your students.")
                                .isRead(false)
                                .build());
                    });

            // C. University Notification
            notifications.add(Notification.builder()
                    .userId(id)
                    .role("UNIVERSITY")
                    .type("RESULT_OUT")
                    .msg("Grading for Exam " + examName + " completed.")
                    .isRead(false)
                    .build());

            analyticsService.generateAnalyticsForExam(Long.valueOf(examId));
            notificationRepository.saveAll(notifications);
            return "Grading and Notifications synced for " + resultsToSave.size() + " students.";
        });
    }

    private ResultEntity calculateSingleResult(QuestionEntity exam, StudentSubmission sub) {
        int marks = 0;
        Map<String, String> answers = sub.getAnswers();

        for (QuestionData q : exam.getQuestions()) {
            String correct = q.getOptions().get(q.getCorrectAnswerIndex());
            String student = answers.get(q.getText());
            if (student != null && student.equalsIgnoreCase(correct)) {
                marks++;
            }
        }

        double percent = ((double) marks / exam.getQuestions().size()) * 100;

        ResultEntity res = new ResultEntity();
        res.setEnrNumber(sub.getEnrNumber());
        res.setExamId(sub.getExamId());
        res.setMarks(marks);
        res.setTotalMarks(exam.getQuestions().size());
        res.setPercentage(percent);
        res.setStatus(percent >= 40 ? "PASSED" : "FAILED");
        return res;
    }

    public ExamReviewDTO getDetailedReview(String examId, String enrNumber) {
        // 1. Fetch all 3 documents
        QuestionEntity exam = questionRepository.findByExamId(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        StudentSubmission submission = submissionRepository.findByEnrNumberAndExamId(enrNumber, examId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        ResultEntity result = resultRepository.findByEnrNumberAndExamId(enrNumber, examId)
                .orElseThrow(() -> new RuntimeException("Result not published yet"));

        //  Build the review for each question
        List<ExamReviewDTO.QuestionReview> reviews = new ArrayList<>();
        Map<String, String> studentAnswers = submission.getAnswers();

        for (QuestionData q : exam.getQuestions()) {
            String correctAnswer = q.getOptions().get(q.getCorrectAnswerIndex());
            String studentAnswer = studentAnswers.getOrDefault(q.getText(), "Not Attempted");

            boolean isCorrect = studentAnswer.equalsIgnoreCase(correctAnswer);

            reviews.add(new ExamReviewDTO.QuestionReview(
                    q.getText(),
                    q.getOptions(),
                    studentAnswer,
                    correctAnswer,
                    isCorrect
            ));
        }

        // 3. Assemble the final DTO
        return ExamReviewDTO.builder()
                .examId(examId)
                .enrNumber(enrNumber)
                .marks(result.getMarks())
                .totalMarks(result.getTotalMarks())
                .percentage(result.getPercentage())
                .status(result.getStatus())
                .questionReviews(reviews)
                .build();
    }
}