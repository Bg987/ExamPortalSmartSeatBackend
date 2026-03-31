package com.example.AiServicesmartSeat.service;

import com.example.AiServicesmartSeat.DTO.ExamDropdownDTO;
import com.example.AiServicesmartSeat.entity.ExamAnalytics;
import com.example.AiServicesmartSeat.entity.ResultEntity;
import com.example.AiServicesmartSeat.repository.*;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AnalyticsService {


    private final SeatAllocationRepo seatRepo; // SQL Repository
    private final ResultRepository resultRepo; // MongoDB Results Repository
    private final ExamAnalyticsRepository analyticsRepo; // MongoDB Analytics Repository
    private final TimetableRepo timetableRepo;
    private final CollegeRepository collegeRepo;


    //for complete exam analytics
    public List<ExamDropdownDTO> getCompletedExamsList() {
        return timetableRepo.findAllCompletedExams();
    }

    @Transactional
    public void generateAnalyticsForExam(Long examId) {
        // 1. Get unique list of College IDs involved in this exam from SQL
        System.out.println("call anaan ");
        List<Long> participatingColleges = seatRepo.findDistinctCollegeIdsByExamId(examId);
        String subId = timetableRepo.findSubjectIdByTimetableId(examId);
        for (Long collegeId : participatingColleges) {
            // 2. Get Participation Counts from SQL (Fast indexed counts)
            int registered = seatRepo.countByTimetableIdAndCollegeCollegeId(examId, collegeId);
            int present = seatRepo.countByTimetableIdAndCollegeCollegeIdAndAttendanceTrue(examId, collegeId);
            int submitted = seatRepo.countByTimetableIdAndCollegeCollegeIdAndIsSubmittedTrue(examId, collegeId);

            // 3. Get Student Enrollment Numbers for this college/exam from SQL
            List<String> collegeStudentEnrNumbers = seatRepo.findEnrollmentNumbersByExamAndCollege(examId, collegeId);

            // 4. Fetch Results from MongoDB using the Enrollment Numbers list
            List<ResultEntity> collegeResults = resultRepo.findByExamIdAndEnrNumberIn(
                    String.valueOf(examId),
                    collegeStudentEnrNumbers
            );

            // 5. Calculate Performance
            long passed = collegeResults.stream()
                    .filter(r -> "PASSED".equalsIgnoreCase(r.getStatus()))
                    .count();

            long failed = collegeResults.stream()
                    .filter(r -> "FAILED".equalsIgnoreCase(r.getStatus()))
                    .count();

            double averageMarks = collegeResults.stream()
                    .mapToInt(ResultEntity::getMarks)
                    .average()
                    .orElse(0.0);

            int highestMarks = collegeResults.stream()
                    .mapToInt(ResultEntity::getMarks)
                    .max()
                    .orElse(0);

            double passPercentage = (present > 0) ? ((double) passed / present) * 100 : 0;

            // 6. Build and Save the Analytics Summary
            ExamAnalytics summary = new ExamAnalytics();

            // Composite ID ensures one record per exam per college
            summary.setId(examId + "_" + collegeId);
            summary.setExamId(String.valueOf(examId));
            summary.setCollegeId(collegeId);
            summary.setCollegeName(collegeRepo.findNameByCollegeId(collegeId));
            summary.setSubjectCode(subId);
            // Set Attendance Stats
            summary.setTotalRegistered(registered);
            summary.setTotalPresent(present);
            summary.setTotalAbsent(registered - present);
            summary.setTotalSubmitted(submitted);
            summary.setAttendancePercentage((registered > 0) ? ((double) present / registered) * 100 : 0);
            // Set Result Stats
            summary.setTotalPassed((int) passed);
            summary.setTotalFailed((int) failed);
            summary.setAverageMarks(averageMarks);
            summary.setHighestMarks(highestMarks);
            summary.setPassPercentage(passPercentage);

            summary.setLastUpdated(new Date());
            Map<String, Integer> breakdown = new HashMap<>();
            int distinction = 0, firstClass = 0, pass = 0, fail = 0;

            for (ResultEntity r : collegeResults) {
                if ("FAILED".equalsIgnoreCase(r.getStatus())) {
                    fail++;
                } else {
                    if (r.getPercentage() >= 70) distinction++;
                    else if (r.getPercentage() >= 60) firstClass++;
                    else pass++;
                }
            }

            breakdown.put("Distinction", distinction);
            breakdown.put("First Class", firstClass);
            breakdown.put("Pass", pass);
            breakdown.put("Fail", fail);

            summary.setGradeBreakdown(breakdown);
            analyticsRepo.save(summary);
        }
    }
}