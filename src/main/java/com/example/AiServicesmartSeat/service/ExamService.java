package com.example.AiServicesmartSeat.service;


import com.example.AiServicesmartSeat.DTO.StudentExamView;
import com.example.AiServicesmartSeat.entity.QuestionEntity;
import com.example.AiServicesmartSeat.entity.SeatAllocation;
import com.example.AiServicesmartSeat.entity.Timetable;
import com.example.AiServicesmartSeat.repository.QuestionRepository;
import com.example.AiServicesmartSeat.repository.SeatAllocationRepo;
import com.example.AiServicesmartSeat.repository.TimetableRepo;
import com.example.AiServicesmartSeat.util.HelperMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ExamService {

    private final TimetableRepo timetableRepo;
    private final QuestionRepository questionRepo;
    private final SeatAllocationRepo seatRepo;
    // REMOVED: private final SeatAllocation allocation; <-- This was the error
    private final HelperMethod helper;

    public Boolean validateStudent(Long examId) {
        String enrNumber = helper.getEnrNumberIdByUserId();

        // Check if student is even allocated to this exam
        if (!seatRepo.existsByStudent_EnrollmentNoAndTimetable_Id(enrNumber, examId)) {
            return null; // Not assigned to this seat
        }

        Timetable timetable = timetableRepo.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        if (timetable.isCompleted() || !timetable.isAccessAllowed()) {
            return false;
        }
        return true;
    }

    public StudentExamView verifyAndGetView(Long examId, String inputPassword) {
        String enrNumber = helper.getEnrNumberIdByUserId();

        // 1. Fetch the specific allocation for THIS student and THIS exam
        SeatAllocation currentAllocation = seatRepo.findByStudent_EnrollmentNoAndTimetable_Id(enrNumber, examId)
                .orElseThrow(() -> new SecurityException("No seat allocation found for this student."));

        // 2. Logic: If attendance is ALREADY true, skip password check
        if (currentAllocation.isAttendance()) {
            return getCachedStudentView(examId);
        }

        // 3. First time entry: Check Password
        QuestionEntity examPaper = questionRepo.findByExamId(String.valueOf(examId))
                .orElseThrow(() -> new RuntimeException("Exam paper not found"));

        if (!inputPassword.equals(examPaper.getExamPassword())) {
            throw new SecurityException("Incorrect 6-digit exam password.");
        }

        // 4. Mark present (flip the flag in DB)
        seatRepo.markAsAttended(enrNumber, examId);

        return getCachedStudentView(examId);
    }

    @Cacheable(value = "activeExams", key = "#examId")
    public StudentExamView getCachedStudentView(Long examId) {
        // ... (Your existing cached logic remains the same)
        Timetable timetable = timetableRepo.findById(examId).get();
        QuestionEntity examPaper = questionRepo.findByExamId(String.valueOf(examId)).get();

        List<StudentExamView.StudentQuestion> cleanQuestions = examPaper.getQuestions().stream()
                .map(q -> new StudentExamView.StudentQuestion(q.getText(), q.getOptions()))
                .collect(Collectors.toList());

        return new StudentExamView(
                examPaper.getExamId(),
                timetable.getSubjectName(),
                timetable.getDurationMinutes(),
                cleanQuestions
        );
    }
}