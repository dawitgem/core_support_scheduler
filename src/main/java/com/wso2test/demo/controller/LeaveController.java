package com.wso2test.demo.controller;

import com.wso2test.demo.model.Leave;
import com.wso2test.demo.model.LeaveRequest;
import com.wso2test.demo.respository.LeaveRepository;
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

    @Autowired
    private LeaveService leaveService;
    
    @Autowired
    private LeaveRepository leaveRepository;


    @PostMapping("/add_leave")
    public ResponseEntity<Leave> addLeave(@RequestBody LeaveRequest leaveRequest) {
        Leave createdLeave = leaveService.addLeave(
                leaveRequest.getEmployeeId(),
                leaveRequest.getLeaveReason(),
                leaveRequest.getStartDate(),
                leaveRequest.getEndDate()
        );

        return new ResponseEntity<>(createdLeave, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Leave>> getAllLeaves() {
        List<Leave> leaves = leaveRepository.findAll();
        return new ResponseEntity<>(leaves, HttpStatus.OK);
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Leave>> getLeavesByEmployeeId(@PathVariable Long employeeId) {
        List<Leave> leaves = leaveService.getLeavesByEmployeeId(employeeId);
        return new ResponseEntity<>(leaves, HttpStatus.OK);
    }

    @GetMapping("/overlapping/{date}")
    public ResponseEntity<List<Leave>> getLeavesOverlappingDate(@PathVariable String date) {
        List<Leave> leaves = leaveService.getLeavesOverlappingDate(LocalDate.parse(date));
        return new ResponseEntity<>(leaves, HttpStatus.OK);
    }

    @PutMapping("/{leaveId}")
    public ResponseEntity<Leave> updateLeave(@PathVariable Long leaveId, @RequestBody Leave leaveDetails) {
        Leave updatedLeave = leaveService.updateLeave(leaveId, leaveDetails);
        return new ResponseEntity<>(updatedLeave, HttpStatus.OK);
    }

    @DeleteMapping("/{leaveId}")
    public ResponseEntity<Void> deleteLeave(@PathVariable Long leaveId) {
        leaveService.deleteLeave(leaveId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteLeaveall() {
        leaveRepository.deleteAll();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }
        
    


    @PutMapping("/{leaveId}/status")
    public ResponseEntity<Leave> updateLeaveStatus(@PathVariable Long leaveId, @RequestParam String status) {
        Leave updatedLeave = leaveService.updateLeaveStatus(leaveId, status);
        return new ResponseEntity<>(updatedLeave, HttpStatus.OK);
    }
}
