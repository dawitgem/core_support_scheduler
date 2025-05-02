package com.wso2test.demo.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.*;

@Entity
@ToString(exclude = "supportSchedules")
@Table(name = "employees")
@Data
@NoArgsConstructor 
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = true)
    private EmployeeStatus status=EmployeeStatus.ACTIVE;  

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = true)
    private EmployeeType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = true)
    private EmployeeLevel level;
   
    @ManyToMany(mappedBy = "employees", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JsonManagedReference
    private List<SupportSchedule> supportSchedules = new ArrayList<>();

    public Employee(String name, String email, String phoneNumber, EmployeeStatus status, EmployeeType type, EmployeeLevel level) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.type = type;
        this.level = level;
    }

   
    
}