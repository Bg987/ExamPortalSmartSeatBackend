package com.example.AiServicesmartSeat.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data // This generates the necessary equals() and hashCode()
@NoArgsConstructor
@AllArgsConstructor
public class SubjectId implements Serializable {
    private String subjectId;
    private String branch;
    private Integer semester; // Now part of the unique identity
}
