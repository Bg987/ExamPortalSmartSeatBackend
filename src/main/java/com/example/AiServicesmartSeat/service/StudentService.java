package com.example.AiServicesmartSeat.service;

import com.example.AiServicesmartSeat.repository.SeatAllocationRepo;
import com.example.AiServicesmartSeat.util.HelperMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class StudentService {

    private final HelperMethod helper;
    private final SeatAllocationRepo seatRepo;

    public List<Map<String, Object>> getExamList(String EnrNumber, boolean status){
        return seatRepo.findAllocatedExamsByStatus(EnrNumber,status);
    }

    public String getEnrNumber() throws Exception {
        return helper.getEnrNumberIdByUserId();
    }

}
