package com.wso2test.demo.respository;

import com.wso2test.demo.model.ShiftType;
import com.wso2test.demo.model.SupportSchedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupportScheduleRepository extends JpaRepository<SupportSchedule, Long> {

    // --- Basic queries ---
    List<SupportSchedule> findByEmployeesId(Long employeeId);
    List<SupportSchedule> findByEmployeesIdAndAssignedDate(Long employeeId, LocalDate assignedDate);
    List<SupportSchedule> findByAssignedDate(LocalDate assignedDate);
    Optional<SupportSchedule> findByAssignedDateAndShiftType(LocalDate assignedDate, ShiftType shiftType);
    List<SupportSchedule> findByShiftTypeAndAssignedDateBetween(ShiftType shiftType, LocalDate startDate, LocalDate endDate);
    List<SupportSchedule> findByAssignedDateBetween(LocalDate startDate, LocalDate endDate);
    boolean existsByAssignedDateBetween(LocalDate startDate, LocalDate endDate);
    void deleteByAssignedDateBetween(LocalDate startDate, LocalDate endDate);

    // --- Weekly limits and fairness ---
    // Count how many times an employee has been scheduled in a week (for fairness checks)
    @Query("SELECT COUNT(s) FROM SupportSchedule s JOIN s.employees e WHERE e.id = :employeeId AND s.assignedDate BETWEEN :startOfWeek AND :endOfWeek AND s.shiftType = 'SUPPORT'")
    int countSupportShiftsInWeek(@Param("employeeId") Long employeeId, @Param("startOfWeek") LocalDate startOfWeek, @Param("endOfWeek") LocalDate endOfWeek);

    // Get all shifts for an employee within a date range
    List<SupportSchedule> findByEmployeesIdAndAssignedDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate);

    // --- Senior/Junior balance check ---
    @Query("SELECT COUNT(e) FROM SupportSchedule s JOIN s.employees e WHERE s.assignedDate = :assignedDate AND s.shiftType = :shiftType AND e.type = :employeeType")
    int countEmployeesByTypeAndShiftOnDate(@Param("assignedDate") LocalDate assignedDate, @Param("shiftType") ShiftType shiftType, @Param("employeeType") com.wso2test.demo.model.EmployeeType employeeType);

    // --- Get all scheduled employees on a specific day/shift ---
    @Query("SELECT e.id FROM SupportSchedule s JOIN s.employees e WHERE s.assignedDate = :assignedDate AND s.shiftType = :shiftType")
    List<Long> findEmployeeIdsByShiftDate(@Param("assignedDate") LocalDate assignedDate, @Param("shiftType") ShiftType shiftType);

    // --- Optional: Detect back-to-back scheduling across days ---
    @Query("SELECT s FROM SupportSchedule s JOIN s.employees e WHERE e.id = :employeeId AND s.assignedDate IN :dates AND s.shiftType = 'SUPPORT'")
    List<SupportSchedule> findConsecutiveSupportShifts(@Param("employeeId") Long employeeId, @Param("dates") List<LocalDate> dates);

    // --- Junior exclusion on COB days ---
    @Query("SELECT s FROM SupportSchedule s JOIN s.employees e WHERE s.shiftType = 'COB' AND s.assignedDate = :date AND e.type = 'JUNIOR'")
    List<SupportSchedule> findJuniorInCobShiftOnDate(@Param("date") LocalDate date);

    List<SupportSchedule> findByAssignedDateAndEmployeesId(LocalDate assignedDate, Long employeeId);
}
