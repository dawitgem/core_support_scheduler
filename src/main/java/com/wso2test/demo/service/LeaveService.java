package com.wso2test.demo.service;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wso2test.demo.model.Employee;
import com.wso2test.demo.model.EmployeeStatus;
import com.wso2test.demo.model.Leave;
import com.wso2test.demo.model.LeaveStatus;
import com.wso2test.demo.respository.EmployeeRepository;
import com.wso2test.demo.respository.LeaveRepository;

@Service
public class LeaveService {

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    public Leave addLeave(Long employeeId, String leaveReason, String startDate, String endDate) {
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new RuntimeException("Employee not found"));

        // Convert startDate and endDate to LocalDate
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        // Create a new Leave object
        Leave leave = new Leave();
        leave.setEmployee(employee);  // Set the fetched employee
        leave.setLeaveReason(leaveReason);
        leave.setStartDate(start);
        leave.setEndDate(end);

        return leaveRepository.save(leave);
    }

    
    public List<Leave> getLeavesByEmployeeId(Long employeeId) {
        return leaveRepository.findByEmployeeId(employeeId);
    }

    public List<Leave> getLeavesOverlappingDate(LocalDate date) {
        return leaveRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(date, date);
    }

    public List<Leave> getLeavesBetweenDates(LocalDate startDate, LocalDate endDate) {
        return leaveRepository.findByStartDateBetween(startDate, endDate);
    }

    public Leave updateLeave(Long leaveId, Leave leaveDetails) {
        Leave existingLeave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new EntityNotFoundException("Leave not found with id: " + leaveId));

        existingLeave.setStartDate(leaveDetails.getStartDate());
        existingLeave.setEndDate(leaveDetails.getEndDate());
        existingLeave.setLeaveReason(leaveDetails.getLeaveReason());
        existingLeave.setEmployee(leaveDetails.getEmployee());
        existingLeave.setStatus(leaveDetails.getStatus()); // Update leave status

        return leaveRepository.save(existingLeave);
    }

    public void deleteLeave(Long leaveId) {
        Leave existingLeave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new EntityNotFoundException("Leave not found with id: " + leaveId));

        Employee employee = existingLeave.getEmployee();
        if (existingLeave.getStatus() == LeaveStatus.APPROVED || existingLeave.getStatus() == LeaveStatus.REJECTED) {
            employee.setStatus(EmployeeStatus.ACTIVE); 
            employeeRepository.save(employee);
        }

        leaveRepository.delete(existingLeave);  
    }

    public Leave updateLeaveStatus(Long leaveId, String status) {
        Leave existingLeave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new EntityNotFoundException("Leave not found with id: " + leaveId));

        LeaveStatus leaveStatus = LeaveStatus.valueOf(status.toUpperCase()); // Convert string status to LeaveStatus enum
        existingLeave.setStatus(leaveStatus);

        Employee employee = existingLeave.getEmployee();
        if (leaveStatus == LeaveStatus.APPROVED) {
            employee.setStatus(EmployeeStatus.ON_LEAVE);
        } else if (leaveStatus == LeaveStatus.REJECTED || leaveStatus == LeaveStatus.CANCELLED) {
            employee.setStatus(EmployeeStatus.ACTIVE);
        }
        employeeRepository.save(employee);

        return leaveRepository.save(existingLeave);
    }
}
