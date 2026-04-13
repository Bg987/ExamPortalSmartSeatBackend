package com.example.AiServicesmartSeat.entity;


import com.example.AiServicesmartSeat.DTO.SubjectId;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subjects")
@IdClass(SubjectId.class) //
@NoArgsConstructor
@Data
public class Subject {

    @Id
    @Column(name = "subjectid")
    private String subjectId;

    @Id
    @Column(name = "branch")
    private String branch;

    @Id
    @Column(name = "semester")
    private Integer semester;

    @Column(name = "subject_name", nullable = false, length = 50)
    private String subjectName;

    @Column(name = "department")
    private String department;

}