package com.example.AiServicesmartSeat.service;

import com.example.AiServicesmartSeat.DTO.ExamSyncDTO;
import com.example.AiServicesmartSeat.DTO.StudentExamView;
import com.example.AiServicesmartSeat.entity.QuestionEntity;
import com.example.AiServicesmartSeat.entity.SeatAllocation;
import com.example.AiServicesmartSeat.entity.Subject;
import com.example.AiServicesmartSeat.entity.Timetable;
import com.example.AiServicesmartSeat.repository.QuestionRepository;
import com.example.AiServicesmartSeat.repository.SeatAllocationRepo;
import com.example.AiServicesmartSeat.repository.SubjectRepository;
import com.example.AiServicesmartSeat.repository.TimetableRepo;
import com.example.AiServicesmartSeat.util.HelperMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ExamService {

    private final TimetableRepo timetableRepo;
    private final QuestionRepository questionRepo;
    private final SeatAllocationRepo seatRepo;
    private final HelperMethod helper;
    private final SubjectRepository subRepo;
    private final ChatClient chatClient;
    private final MongoTemplate mongoTemplate;

    public Boolean validateStudent(Long examId) {
        String enrNumber = helper.getEnrNumberIdByUserId();

        // Check if student is even allocated to this exam
        if (!seatRepo.existsByStudent_EnrollmentNoAndTimetable_Id(enrNumber, examId)) {
            return null; // Not assigned seat to student for this exam
        }

        Timetable timetable = timetableRepo.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        if (timetable.isCompleted() || !timetable.isAccessAllowed()) {
            return false;
        }
        return true;
    }

    public boolean checkCompletionStatus(Long examId) {
        // If the exam exists, return its status; otherwise, assume not completed (false)
        return timetableRepo.existsByIdAndCompletedTrue(examId);
    }

    public StudentExamView verifyAndGetView(Long examId, String inputPassword) {
        String enrNumber = helper.getEnrNumberIdByUserId();

        //Fetch the specific allocation for THIS student and THIS exam
        SeatAllocation currentAllocation = seatRepo.findByStudent_EnrollmentNoAndTimetable_Id(enrNumber, examId)
                .orElseThrow(() -> new SecurityException("No seat allocation found for this student."));

        if (currentAllocation.isSubmitted()) {
            throw new IllegalArgumentException("Exam is over. You have already submitted your response.");
        }

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
                timetable.getStartTime(),
                cleanQuestions
        );
    }

    @Async("examTaskExecutor")
    public void processSyncRequest(ExamSyncDTO dto, String enrNumber,String status) {
        // Find by Student + Exam
        Query query = new Query(Criteria.where("enrNumber").is(enrNumber)
                .and("examId").is(dto.getExamId()));

        LocalDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toLocalDateTime();
        // Overwrite the answers map
        Update update = new Update()
                .set("answers", dto.getAnswers())
                .set("lastSynced",now)
                .set("status",status);

        // Perform the write to MongoDB
        mongoTemplate.upsert(query, update, "student_submissions");
        if(status.equals("COMPLETE")){
            seatRepo.markAsSubmitted(enrNumber, Long.valueOf(dto.getExamId()));
        }
        // Optional: Log for your own debugging
        System.out.println("Async Sync Complete for: " + enrNumber);
    }

    public String generateSemesterDraft(Integer semester, String startDate, String startTime) {
        List<Subject> subjects = subRepo.findBySemester(semester);

        StringBuilder dataList = new StringBuilder();
        for (Subject s : subjects) {
            dataList.append(String.format("- ID: %s, Name: %s, BRANCH: %s\n",
                    s.getSubjectId(), s.getSubjectName(), s.getBranch()));
        }

        String prompt = String.format("""
        Act as an University Exam Scheduler for Semester %d.
        
        UNIVERSITY CONSTRAINTS:
        - Exam Period Starts On: %s
        - Standard Start Time: %s
        - Duration: 180 minutes
        
        SUBJECTS TO ALLOCATE:
        %s
        
        STRICT SCHEDULING RULES:
        1. All subjects with the same 'subjectId' MUST be scheduled on the same 'examDate' and 'startTime'.
        2. Every branch must have a 1-day gap (e.g., if Computer Branch has an exam on 2026-06-10, its next exam must be 2026-06-12 or later).
        3. You must use the 'examDate' format YYYY-MM-DD.
        4. The 'startTime' must be exactly '%s' (Format HH:mm).
        5. The 'duration' must be exactly 180.
        
        OUTPUT INSTRUCTIONS:
        Output ONLY a valid raw JSON array of objects. Do not include markdown formatting or extra text.
        
        REQUIRED JSON STRUCTURE:
        [
          {
            "subjectId": "String",
            "subjectName": "String",
            "branch": "String",
            "semester": %d,
            "examDate": "YYYY-MM-DD",
            "startTime": "HH:mm",
            "duration": 180
          }
        ]
        """, semester, startDate, startTime, dataList.toString(), startTime, semester);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content()
                .replace("```json", "")
                .replace("```", "")
                .trim();
    }
}