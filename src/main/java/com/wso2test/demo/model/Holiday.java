package com.wso2test.demo.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;


import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.Data;
@Entity
@Table(name = "holiday")
@Data
public class Holiday {

    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "date", nullable = false, unique = true)
    private LocalDate date;

   
    
}
