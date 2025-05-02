package com.wso2test.demo.service;

import com.wso2test.demo.model.Employee;
import com.wso2test.demo.model.EmployeeStatus;
import com.wso2test.demo.model.Leave;
import com.wso2test.demo.model.LeaveStatus;
import com.wso2test.demo.respository.EmployeeRepository;
import com.wso2test.demo.respository.LeaveRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;

@Service
public class LeaveService {

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public LeaveService(LeaveRepository leaveRepository, EmployeeRepository employeeRepository) {
        this.leaveRepository = leaveRepository;
        this.employeeRepository = employeeRepository;
    }

    // Add new leave request
    public Leave addLeave(Long employeeId, String leaveReason, String startDate, String endDate) {
        Employee employee = getEmployeeById(employeeId);
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        Leave leave = new Leave();
        leave.setEmployee(employee);
        leave.setLeaveReason(leaveReason);
        leave.setStartDate(start);
        leave.setEndDate(end);

        return leaveRepository.save(leave);
    }

    // Get all leaves for a given employee
    public List<Leave> getLeavesByEmployeeId(Long employeeId) {
        return leaveRepository.findByEmployeeId(employeeId);
    }

    // Get leaves overlapping a specific date
    public List<Leave> getLeavesOverlappingDate(LocalDate date) {
        return leaveRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(date, date);
    }

    // Get leaves within a date range
    public List<Leave> getLeavesBetweenDates(LocalDate startDate, LocalDate endDate) {
        return leaveRepository.findByStartDateBetween(startDate, endDate);
    }

    // Update a leave request
    public Leave updateLeave(Long leaveId, Leave updatedLeave) {
        Leave existingLeave = getLeaveById(leaveId);

        existingLeave.setStartDate(updatedLeave.getStartDate());
        existingLeave.setEndDate(updatedLeave.getEndDate());
        existingLeave.setLeaveReason(updatedLeave.getLeaveReason());
        existingLeave.setEmployee(updatedLeave.getEmployee());
        existingLeave.setStatus(updatedLeave.getStatus());

        return leaveRepository.save(existingLeave);
    }

    // Delete a leave and reset employee status if necessary
    public void deleteLeave(Long leaveId) {
        Leave leave = getLeaveById(leaveId);
        Employee employee = leave.getEmployee();

        if (leave.getStatus() == LeaveStatus.APPROVED || leave.getStatus() == LeaveStatus.REJECTED) {
            employee.setStatus(EmployeeStatus.ACTIVE);
            employeeRepository.save(employee);
        }

        leaveRepository.delete(leave);
    }

    // Approve, reject, or cancel a leave and update employee status accordingly
    public Leave updateLeaveStatus(Long leaveId, String status) {
        Leave leave = getLeaveById(leaveId);
        LeaveStatus leaveStatus;

        try {
            leaveStatus = LeaveStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid leave status: " + status);
        }

        leave.setStatus(leaveStatus);

        Employee employee = leave.getEmployee();
        if (leaveStatus == LeaveStatus.APPROVED) {
            employee.setStatus(EmployeeStatus.ON_LEAVE);
        } else if (leaveStatus == LeaveStatus.REJECTED || leaveStatus == LeaveStatus.CANCELLED) {
            employee.setStatus(EmployeeStatus.ACTIVE);
        }

        employeeRepository.save(employee);
        return leaveRepository.save(leave);
    }

    // --- Helper methods ---
    private Leave getLeaveById(Long leaveId) {
        return leaveRepository.findById(leaveId)
                .orElseThrow(() -> new EntityNotFoundException("Leave not found with ID: " + leaveId));
    }

    private Employee getEmployeeById(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));
    }
}
