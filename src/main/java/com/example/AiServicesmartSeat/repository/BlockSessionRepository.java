package com.example.AiServicesmartSeat.repository;


import com.example.AiServicesmartSeat.entity.BlockSession;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

public interface BlockSessionRepository extends JpaRepository<BlockSession, Long> {
    Optional<BlockSession> findByEnrNumber(String enrNumber);

    @Transactional // 1. Ensures the delete happens within a database transaction
    @Modifying// 2. Tells Hibernate this is an UPDATE/DELETE, not a SELECT
    void deleteByEnrNumber(String enrNumber);
}
