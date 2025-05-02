package com.wso2test.demo.controller;

import com.wso2test.demo.model.SupportSchedule;
import com.wso2test.demo.respository.SupportScheduleRepository;
import com.wso2test.demo.service.SupportSchedulerService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class SchedulerController {
    @Autowired
    private final SupportSchedulerService supportSchedulerService;

    @Autowired
    private final SupportScheduleRepository supportScheduleRepository;
    
    // Endpoint to generate the monthly schedule
    @PostMapping("/generate")
    public ResponseEntity<String> generateMonthlySchedule(
            @RequestParam int year, 
            @RequestParam int month,
            @RequestParam(required = false) List<String> excludeJuniorDates) {

        try {
            // Pass the excludeJuniorDates parameter to the service if provided
            supportSchedulerService.generateMonthlySchedule(year, month, excludeJuniorDates);
            return ResponseEntity.ok("Schedule generated successfully for " + year + "-" + month);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error generating schedule: " + e.getMessage());
        }
    }
 
    // Endpoint to retrieve all support schedules
    @GetMapping
    public ResponseEntity<List<SupportSchedule>> getAllSchedules() {
        List<SupportSchedule> schedules = supportScheduleRepository.findAll();
        return ResponseEntity.ok(schedules);
    }

    // Endpoint to delete all schedules (used when resetting or testing)
    @DeleteMapping
    public ResponseEntity<Void> deleteAllSchedules() {
        supportScheduleRepository.deleteAll();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Endpoint to check if all employees have been assigned shifts in the current cycle
    // @GetMapping("/check-assignments")
    // public ResponseEntity<String> checkEmployeeAssignments() {
    //     try {
    //         boolean allAssigned = supportSchedulerService.allEmployeesAssigned();
    //         if (allAssigned) {
    //             return ResponseEntity.ok("All employees have been assigned shifts.");
    //         } else {
    //             return ResponseEntity.ok("Not all employees have been assigned shifts yet.");
    //         }
    //     } catch (Exception e) {
    //         return ResponseEntity.status(500).body("Error checking assignments: " + e.getMessage());
    //     }
    // }
}
