package com.example.AiServicesmartSeat.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "block_session")
@Data                 // Generates Getters, Setters, toString, etc.
@NoArgsConstructor    // Generates a blank constructor (Required by JPA)
@AllArgsConstructor   // Generates a constructor with all fields
public class BlockSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String enrNumber;

    @Column(nullable = false)
    private LocalDateTime blockedAt;

    // Custom constructor for easier use in your Service
    public BlockSession(String enrNumber, LocalDateTime blockedAt) {
        this.enrNumber = enrNumber;
        this.blockedAt = blockedAt;
    }
}
