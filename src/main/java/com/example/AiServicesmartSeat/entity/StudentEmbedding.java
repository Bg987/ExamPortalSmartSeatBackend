package com.example.AiServicesmartSeat.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


@Data
@Entity
@Table(name = "student_embeddings")
public class StudentEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", unique = true, nullable = false)
    private Long studentId;

    // We map it as a String to prevent the StreamCorruptedException
    @Column(name = "face_embedding", columnDefinition = "vector(512)")
    private String faceEmbedding;

    /**
     * Helper to convert the DB String "[0.1, -0.2...]" into a float[]
     */
    public float[] getEmbeddingAsArray() {
        if (this.faceEmbedding == null || this.faceEmbedding.isEmpty()) {
            return new float[0];
        }
        // Remove brackets and split by comma
        String clean = this.faceEmbedding.replace("[", "").replace("]", "");
        String[] parts = clean.split(",");
        float[] vector = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            vector[i] = Float.parseFloat(parts[i].trim());
        }
        return vector;
    }
}