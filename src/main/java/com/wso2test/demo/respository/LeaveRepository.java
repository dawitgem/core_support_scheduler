package com.wso2test.demo.respository;

import com.wso2test.demo.model.Employee;
import com.wso2test.demo.model.Leave;
import com.wso2test.demo.model.LeaveStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {

    // Get all leaves for a specific employee
    List<Leave> findByEmployeeId(Long employeeId);

    // Check if an employee is on leave during a specific date
    List<Leave> findByEmployeeIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Long employeeId, LocalDate date1, LocalDate date2);

    // Get all leaves overlapping with a specific day
    List<Leave> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate date, LocalDate date2);

    // Get leaves that fall within a date range
    List<Leave> findByStartDateBetween(LocalDate startDate, LocalDate endDate);

    // Optional: Find all approved leaves for a specific employee
    List<Leave> findByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);

        boolean existsByEmployeeAndStatus(Employee employee, LeaveStatus status);

    // Optional: Check all active leaves (PENDING or APPROVED) overlapping with a specific date
    @Query("SELECT l FROM Leave l WHERE :date BETWEEN l.startDate AND l.endDate AND l.status <> 'REJECTED'")
    List<Leave> findActiveLeavesOnDate(@Param("date") LocalDate date);

    // Optional: Check if an employee is on leave on a specific date
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Leave l WHERE l.employee.id = :employeeId AND :date BETWEEN l.startDate AND l.endDate AND l.status = 'APPROVED'")
    boolean isEmployeeOnLeave(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);

     // *** NEWLY ADDED METHOD ***
    // Find leaves for a specific employee that start and end on the exact given dates.
    // Useful for checking if a single-day leave record exists for a specific day.
    List<Leave> findByEmployeeIdAndStartDateAndEndDate(Long employeeId, LocalDate startDate, LocalDate endDate);

    boolean existsByEmployeeIdAndStartDateAndEndDateAndLeaveReasonAndStatusIn(
        Long employeeId, 
        LocalDate startDate, 
        LocalDate endDate, 
        String leaveReason, 
        Collection<LeaveStatus> statuses // Use Collection for 'IN' clause
    );

}
