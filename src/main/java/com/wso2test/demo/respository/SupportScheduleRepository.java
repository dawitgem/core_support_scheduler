package com.wso2test.demo.respository;

import com.wso2test.demo.model.SupportSchedule;
import com.wso2test.demo.model.ShiftType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupportScheduleRepository extends JpaRepository<SupportSchedule, Long> {

    // Query schedules by employeeId and a specific assigned date
    List<SupportSchedule> findByEmployeesIdAndAssignedDate(Long employeeId, LocalDate assignedDate);

    // Query schedules by employeeId, assigned date, and shift type
    List<SupportSchedule> findByEmployeesIdAndAssignedDateAndShiftType(Long employeeId, LocalDate assignedDate, ShiftType shiftType);

    // Query schedules by date range
    List<SupportSchedule> findByAssignedDateBetween(LocalDate startDate, LocalDate endDate);

    // Query schedules by shift type and date range
    List<SupportSchedule> findByShiftTypeAndAssignedDateBetween(ShiftType shiftType, LocalDate startDate, LocalDate endDate);

    // Query schedules by shift type
    List<SupportSchedule> findByShiftType(ShiftType shiftType);

    // Query schedules by employeeId and shift type
    List<SupportSchedule> findByEmployeesIdAndShiftType(Long employeeId, ShiftType shiftType);

    // Query schedules by assigned date
    List<SupportSchedule> findByAssignedDate(LocalDate assignedDate);

    // Query schedules by assigned date and shift type
    Optional<SupportSchedule> findByAssignedDateAndShiftType(LocalDate assignedDate, ShiftType shiftType);


    // Check if any schedule exists within a date range
    boolean existsByAssignedDateBetween(LocalDate startDate, LocalDate endDate);

    // Delete schedules by date range
    void deleteByAssignedDateBetween(LocalDate startDate, LocalDate endDate);

    // Query schedules by employeeId within a date range
    List<SupportSchedule> findByEmployeesIdAndAssignedDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate);

    // Query schedules by employeeId
    List<SupportSchedule> findByEmployeesId(Long employeeId);

    // Query schedules by shift type and assigned date
    List<SupportSchedule> findAllByShiftTypeAndAssignedDate(ShiftType shiftType, LocalDate assignedDate);
}
