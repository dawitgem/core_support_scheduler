package com.wso2test.demo.model;

import javax.persistence.*;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.wso2test.demo.util.LocalDateDeserializer;

import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "leaves")
@Data
public class Leave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee; 

    @Column(name = "leave_reason", nullable = false)
    private String leaveReason;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate; 

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LeaveStatus status = LeaveStatus.PENDING;
}
