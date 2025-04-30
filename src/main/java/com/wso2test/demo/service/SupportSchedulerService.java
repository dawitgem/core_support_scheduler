package com.wso2test.demo.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wso2test.demo.model.Employee;
import com.wso2test.demo.model.EmployeeStatus;
import com.wso2test.demo.model.Leave;
import com.wso2test.demo.model.LeaveStatus;
import com.wso2test.demo.model.ShiftType;
import com.wso2test.demo.model.SupportSchedule;
import com.wso2test.demo.respository.EmployeeRepository;
import com.wso2test.demo.respository.LeaveRepository;
import com.wso2test.demo.respository.SupportScheduleRepository;

import lombok.RequiredArgsConstructor;
import lombok.var;

@Service
@RequiredArgsConstructor
@Transactional
public class SupportSchedulerService {
    @Autowired
    private  EmployeeRepository employeeRepository;

    @Autowired
    private  SupportScheduleRepository supportScheduleRepository;

    @Autowired
    private  LeaveRepository leaveRepository;


    // Generate the schedule for the month
    public void generateMonthlySchedule(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(5);
        LocalDate endDate = yearMonth.atEndOfMonth().minusDays(21);


        boolean alreadyGenerated = supportScheduleRepository.existsByAssignedDateBetween(startDate, endDate);
        if (alreadyGenerated) {
            throw new IllegalStateException("Schedule for " + year + "-" + month + " has already been generated.");
        }

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                generateDailySchedule(date);
            }
        }
    }

    // Generate daily schedule for each date
    private void generateDailySchedule(LocalDate scheduleDate) {
        List<Employee> allEmployees = employeeRepository.findAll()
                .stream()
                .filter(e -> e.getStatus() == EmployeeStatus.ACTIVE)
                .collect(Collectors.toList());


        if (allEmployees.isEmpty()) {
            throw new RuntimeException("No active employees available.");
        }

        // Track employees who have already been assigned the shift type (Support or COB)
        List<Employee> eligibleForSupport = filterEligibleEmployees(allEmployees, ShiftType.SUPPORT,scheduleDate);
        List<Employee> eligibleForCob = filterEligibleEmployees(allEmployees, ShiftType.COB,scheduleDate);




        Collections.shuffle(eligibleForSupport);
        Collections.shuffle(eligibleForCob);

        // Select employees for Support and COB shifts
        List<Employee> selectedSupportEmployees = pickEmployees(eligibleForSupport, 2);
        List<Employee> selectedCobEmployees = pickEmployees(eligibleForCob, 2);


        // Assign the shifts
        for (Employee employee : selectedSupportEmployees) {
            assignShift(scheduleDate, employee, ShiftType.SUPPORT);
        }

        // for (Employee employee : selectedCobEmployees) {
        //     assignShift(scheduleDate, employee, ShiftType.COB);

        //     // Assign a leave day for COB employees after their shift
        //     Leave leave = new Leave();
        //     leave.setEmployee(employee);
        //     employee.setStatus(EmployeeStatus.ON_LEAVE);
        //     employeeRepository.save(employee);
        //     leave.setStartDate(scheduleDate.plusDays(1));
        //     leave.setEndDate(scheduleDate.plusDays(1));
        //     leave.setStatus(LeaveStatus.APPROVED);
        //     leave.setLeaveReason("Rest after COB shift");
        //     leaveRepository.save(leave);
        // }

        // // Once all employees have been assigned a shift, reset their eligibility for the next round
    }

   private List<Employee> filterEligibleEmployees(List<Employee> allEmployees, ShiftType shiftType, LocalDate scheduleDate) {
    LocalDate startOfMonth = scheduleDate.withDayOfMonth(1);
    LocalDate endOfMonth = scheduleDate.withDayOfMonth(scheduleDate.lengthOfMonth());

    // Get all shift assignments of the month for the given shift type
    List<SupportSchedule> currentMonthSchedules = supportScheduleRepository
            .findByAssignedDateBetween(startOfMonth, endOfMonth)
            .stream()
            .filter(s -> s.getShiftType() == shiftType)
            .collect(Collectors.toList());

    // Track employees who were already assigned to this shift type
    Set<Long> alreadyAssignedIds = currentMonthSchedules.stream()
            .flatMap(schedule -> schedule.getEmployees().stream())
            .map(Employee::getId)
            .collect(Collectors.toSet());

    // Find employees who haven't yet been assigned the shift
    List<Employee> notYetAssigned = allEmployees.stream()
            .filter(e -> !alreadyAssignedIds.contains(e.getId()))
            .collect(Collectors.toList());

    List<Employee> eligibleEmployees;

    if (!notYetAssigned.isEmpty()) {
        // Only allow employees who haven't had a turn yet
        eligibleEmployees = notYetAssigned;
    } else {
        // All employees had their turn — reset and allow all
        eligibleEmployees = allEmployees;
    }

    // Avoid assigning employees who worked yesterday or the day before
    Set<Long> recentlyWorked = new HashSet<>();

    supportScheduleRepository.findByAssignedDate(scheduleDate.minusDays(1)).forEach(schedule ->
        schedule.getEmployees().forEach(emp -> recentlyWorked.add(emp.getId()))
    );

    supportScheduleRepository.findByAssignedDate(scheduleDate.minusDays(2)).forEach(schedule ->
        schedule.getEmployees().forEach(emp -> recentlyWorked.add(emp.getId()))
    );

    // Final filtering: must not have worked recently
    return eligibleEmployees.stream()
            .filter(e -> !recentlyWorked.contains(e.getId()))
            .collect(Collectors.toList());
}

    
    
    

    // Pick employees from the list for the shift
    private List<Employee> pickEmployees(List<Employee> eligibleEmployees, int desiredCount) {
        if (eligibleEmployees.isEmpty()) {
            return new ArrayList<>();
        }
        if (eligibleEmployees.size() <= desiredCount) {
            return new ArrayList<>(eligibleEmployees);
        }
        return eligibleEmployees.subList(0, desiredCount);
    }

    // Assign a shift to an employee
    private void assignShift(LocalDate date, Employee employee, ShiftType shiftType) {
        // Try to find existing schedule for the date and shift type
        SupportSchedule schedule = supportScheduleRepository
                .findByAssignedDateAndShiftType(date, shiftType)
                .orElseGet(() -> {
                    SupportSchedule newSchedule = new SupportSchedule();
                    newSchedule.setAssignedDate(date);
                    newSchedule.setShiftType(shiftType);
                    return newSchedule;
                });
    
        // Prevent duplicates
        if (!schedule.getEmployees().contains(employee)) {
            schedule.getEmployees().add(employee);
        }
    
        if (!employee.getSupportSchedules().contains(schedule)) {
            employee.getSupportSchedules().add(schedule);
        }
    
        supportScheduleRepository.save(schedule); // Will also update join table
    }
   

    // Check if all employees have been assigned a shift in the current cycle
    public boolean allEmployeesAssigned() {
        List<Employee> allEmployees = employeeRepository.findAll()
                .stream()
                .filter(e -> e.getStatus() == EmployeeStatus.ACTIVE)
                .collect(Collectors.toList());
    
        YearMonth currentMonth = YearMonth.from(LocalDate.now());
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();
    
        for (Employee employee : allEmployees) {
            List<SupportSchedule> schedules = supportScheduleRepository
                    .findByEmployeesIdAndAssignedDateBetween(employee.getId(), startOfMonth, endOfMonth);
    
            if (schedules.isEmpty()) {
                return false; // This employee has not been assigned in this cycle
            }
        }
    
        return true;
    }
}
