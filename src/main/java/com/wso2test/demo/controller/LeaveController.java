package com.wso2test.demo.controller;

import com.wso2test.demo.model.Leave;
import com.wso2test.demo.model.LeaveRequest;
import com.wso2test.demo.service.LeaveService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    private final LeaveService leaveService;

    @Autowired
    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    // Add a new leave request
    @PostMapping
    public ResponseEntity<Leave> createLeave(@RequestBody LeaveRequest leaveRequest) {
        Leave leave = leaveService.addLeave(
                leaveRequest.getEmployeeId(),
                leaveRequest.getLeaveReason(),
                leaveRequest.getStartDate(),
                leaveRequest.getEndDate()
        );
        return new ResponseEntity<>(leave, HttpStatus.CREATED);
    }

    // Get all leaves
    @GetMapping
    public ResponseEntity<List<Leave>> getAllLeaves() {
        List<Leave> leaves = leaveService.getLeavesBetweenDates(LocalDate.MIN, LocalDate.MAX);
        return new ResponseEntity<>(leaves, HttpStatus.OK);
    }

    // Get all leaves for a specific employee
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Leave>> getLeavesByEmployee(@PathVariable Long employeeId) {
        List<Leave> leaves = leaveService.getLeavesByEmployeeId(employeeId);
        return new ResponseEntity<>(leaves, HttpStatus.OK);
    }

    // Get all leaves that overlap a specific date
    @GetMapping("/overlapping/{date}")
    public ResponseEntity<List<Leave>> getLeavesOverlapping(@PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date);
        List<Leave> leaves = leaveService.getLeavesOverlappingDate(localDate);
        return new ResponseEntity<>(leaves, HttpStatus.OK);
    }

    // Update an existing leave request
    @PutMapping("/{leaveId}")
    public ResponseEntity<Leave> updateLeave(@PathVariable Long leaveId, @RequestBody Leave leaveDetails) {
        Leave updatedLeave = leaveService.updateLeave(leaveId, leaveDetails);
        return new ResponseEntity<>(updatedLeave, HttpStatus.OK);
    }

    // Delete a leave by ID
    @DeleteMapping("/{leaveId}")
    public ResponseEntity<Void> deleteLeave(@PathVariable Long leaveId) {
        leaveService.deleteLeave(leaveId);
        return ResponseEntity.noContent().build();
    }

    // Update the status of a leave
    @PutMapping("/{leaveId}/status")
    public ResponseEntity<Leave> updateLeaveStatus(@PathVariable Long leaveId, @RequestParam String status) {
        Leave updatedLeave = leaveService.updateLeaveStatus(leaveId, status);
        return new ResponseEntity<>(updatedLeave, HttpStatus.OK);
    }

    // Optional: Clear all leaves (admin/debug only)
    @DeleteMapping
    public ResponseEntity<Void> deleteAllLeaves() {
        // In real apps, restrict this to admin use only!
        leaveService.getLeavesBetweenDates(LocalDate.MIN, LocalDate.MAX).forEach(leave ->
                leaveService.deleteLeave(leave.getId())
        );
        return ResponseEntity.noContent().build();
    }
}
