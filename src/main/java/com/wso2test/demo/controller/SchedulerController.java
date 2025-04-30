package com.wso2test.demo.controller;

import com.wso2test.demo.model.Employee;
import com.wso2test.demo.model.SupportSchedule;
import com.wso2test.demo.respository.SupportScheduleRepository;
import com.wso2test.demo.service.SupportSchedulerService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class SchedulerController {

    private final SupportSchedulerService supportSchedulerService;
    private final SupportScheduleRepository supportScheduleRepository;
    
    @PostMapping("/generate")
    public ResponseEntity<String> generateMonthlySchedule(
            @RequestParam int year, 
            @RequestParam int month) {

        try {
            supportSchedulerService.generateMonthlySchedule(year, month);
            return ResponseEntity.ok("Schedule generated successfully for " + year + "-" + month);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error generating schedule: " + e.getMessage());
        }
    }
 
    @GetMapping
    public ResponseEntity<List<SupportSchedule>> getAllSchedules() {
        List<SupportSchedule> schedules = supportScheduleRepository.findAll();
        return ResponseEntity.ok(schedules);
    }
    

  @DeleteMapping
    public ResponseEntity<Void> deleteLeaveall() {
        supportScheduleRepository.deleteAll();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }
        
    

 
    // Endpoint to check if all employees have been assigned a shift in the current cycle
    @GetMapping("/check-assignments")
    public ResponseEntity<String> checkEmployeeAssignments() {
        try {
            boolean allAssigned = supportSchedulerService.allEmployeesAssigned();
            if (allAssigned) {
                return ResponseEntity.ok("All employees have been assigned shifts.");
            } else {
                return ResponseEntity.ok("Not all employees have been assigned shifts yet.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error checking assignments: " + e.getMessage());
        }
    }
}
