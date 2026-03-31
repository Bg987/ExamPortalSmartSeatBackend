package com.example.AiServicesmartSeat.repository;

import com.example.AiServicesmartSeat.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface  CollegeRepository  extends JpaRepository<College,Long> {


    Optional<College> findByUser_userId(Long userId);

    @Query(value = "SELECT name FROM colleges WHERE college_id = :collegeId", nativeQuery = true)
    String findNameByCollegeId(@Param("collegeId") Long collegeId);
}
