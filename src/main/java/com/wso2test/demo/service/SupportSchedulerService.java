package com.wso2test.demo.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wso2test.demo.model.*;
import com.wso2test.demo.respository.*;

@Service
@Transactional
public class SupportSchedulerService {

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private SupportScheduleRepository supportScheduleRepository;
    @Autowired private LeaveRepository leaveRepository;
    @Autowired private HolidayRepository holidayRepository;

    // Generate the schedule for the month
    public void generateMonthlySchedule(int year, int month, List<String> excludeJuniorsFromCob) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        

        // Check if the schedule already exists for this period
        if (supportScheduleRepository.existsByAssignedDateBetween(startDate, endDate)) {
            throw new IllegalStateException("Schedule already exists for this period.");
        }

        // Get holidays for the month
        Set<LocalDate> holidays = holidayRepository.findAll().stream()
                .map(Holiday::getDate)
                .collect(Collectors.toSet());

        // Track unavailability and consecutive Sundays
        Set<Long> cobUnavailable = new HashSet<>();
        Set<Long> seniorAssignmentsTracker = new HashSet<>();
        Set<LocalDate> systemMonitorSundaysTracker = new HashSet<>();

        Map<LocalDate, List<Employee>> saturdayCobAssignments = new HashMap<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {

            // Skip holidays for shifts but not for system monitor shifts
            if (holidays.contains(date)) continue;

            boolean isSunday = date.getDayOfWeek() == DayOfWeek.SUNDAY;
            boolean isSaturday = date.getDayOfWeek() == DayOfWeek.SATURDAY;

            // Generate COB shifts first (skip Sundays)
            if (!isSunday) {
                List<Employee> availableForCob = getAvailableEmployees(ShiftType.COB, date, cobUnavailable);
                
                // Exclude juniors if the date is included in the input list
                if (excludeJuniorsFromCob.contains(date)) {
                    availableForCob = availableForCob.stream()
                            .filter(e -> e.getLevel() != EmployeeLevel.JUNIOR)
                            .collect(Collectors.toList());
                }
                
                List<Employee> selectedCobEmployees = pickEmployeesForCob(availableForCob, 3);
                for (Employee emp : selectedCobEmployees) {
                    assignShift(date, emp, ShiftType.COB);
                    cobUnavailable.add(emp.getId()); // Mark them unavailable for the next day
                    assignLeaveAfterCob(emp, date.plusDays(1)); // Give them leave for the next day
                }
                
            if (isSaturday) {
                // Assign COB shifts fairly on Saturdays (round-robin or fair distribution)
                selectedCobEmployees = pickFairCobEmployeesForSaturday(availableForCob, saturdayCobAssignments, date);
            } else {
                selectedCobEmployees = pickEmployeesForCob(availableForCob, 3);
            }

            for (Employee emp : selectedCobEmployees) {
                assignShift(date, emp, ShiftType.COB);
                cobUnavailable.add(emp.getId()); // Mark them unavailable for the next day
                assignLeaveAfterCob(emp, date.plusDays(1)); // Give them leave for the next day
            }
        }
            
            // Generate Support shifts (must include 1 senior employee)
            if (!isSunday) {
                List<Employee> availableForSupport = getAvailableEmployees(ShiftType.SUPPORT, date, cobUnavailable);
                Employee seniorSupport = getSeniorEmployeeForSupport(availableForSupport, seniorAssignmentsTracker);
                List<Employee> selectedSupportEmployees = pickEmployeesForSupport(availableForSupport, 2);

                // Ensure 1 senior and 2 juniors for Support shift
                if (seniorSupport != null) {
                    assignShift(date, seniorSupport, ShiftType.SUPPORT);
                    seniorAssignmentsTracker.add(seniorSupport.getId());  // Track senior's turn
                }

                // Assign 2 juniors for the Support shift
                for (Employee emp : selectedSupportEmployees) {
                    assignShift(date, emp, ShiftType.SUPPORT);
                }
            }


            // Generate System Monitor shifts (assigned every day)
            if (isSunday && !systemMonitorSundaysTracker.contains(date)) {
                List<Employee> availableForSystemMonitor = getAvailableEmployees(ShiftType.SYSTEM_MONITOR, date, cobUnavailable);
                List<Employee> selectedSystemMonitors = pickEmployeesForSystemMonitor(availableForSystemMonitor, 1);
                for (Employee emp : selectedSystemMonitors) {
                    assignShift(date, emp, ShiftType.SYSTEM_MONITOR);
                }
                systemMonitorSundaysTracker.add(date);  // Prevent consecutive Sunday assignments
            }
        }
    }

    // Get available employees for a shift type considering unavailability (e.g., COB yesterday)
    private List<Employee> getAvailableEmployees(ShiftType shiftType, LocalDate date, Set<Long> unavailableEmployees) {
        List<Employee> allEmployees = employeeRepository.findAll()
                .stream()
                .filter(e -> e.getStatus() == EmployeeStatus.ACTIVE)
                .collect(Collectors.toList());

        // Filter employees who are available (not on leave, etc.)
        return allEmployees.stream()
                .filter(e -> !unavailableEmployees.contains(e.getId()))
                .filter(e -> !hasWorkedOnPreviousDay(e, date))  // Prevent assignments if they worked previous day
                .filter(e -> !isOnLeave(e, date))  // Skip if they are on leave
                .collect(Collectors.toList());
    }

    // Check if an employee worked the previous day
    private boolean hasWorkedOnPreviousDay(Employee employee, LocalDate date) {
        List<SupportSchedule> schedules = supportScheduleRepository.findByAssignedDateAndEmployeesId(date.minusDays(1), employee.getId());
        return !schedules.isEmpty(); // If the employee was scheduled the day before
    }

    // Check if an employee is on leave for the specific date
    private boolean isOnLeave(Employee employee, LocalDate date) {
        return leaveRepository.existsByEmployeeAndStatus(employee, LeaveStatus.APPROVED) &&
                leaveRepository.findByEmployeeIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(employee.getId(), date, date).size() > 0;
    }

    // Get the next available senior employee for the Support shift (round-robin assignment)
    private Employee getSeniorEmployeeForSupport(List<Employee> employees, Set<Long> seniorAssignmentsTracker) {
        // Filter seniors and return the next one in round-robin fashion
        List<Employee> seniors = employees.stream()
                .filter(e -> e.getLevel() == EmployeeLevel.SENIOR)
                .collect(Collectors.toList());

        if (seniors.isEmpty()) return null;  // No seniors available

        // Find the first senior who hasn't been assigned recently or is on leave
        for (Employee senior : seniors) {
            if (!seniorAssignmentsTracker.contains(senior.getId()) && !isOnLeave(senior, LocalDate.now())) {
                return senior;
            }
        }
        return seniors.get(0);  // If all seniors have been assigned or are on leave, reset to first senior
    }

    // Pick COB employees (3 employees, prioritize seniors)
    private List<Employee> pickEmployeesForCob(List<Employee> availableEmployees, int desiredCount) {
        if (availableEmployees.isEmpty()) return new ArrayList<>();
        
        // Prioritize COB performers for the shift
        List<Employee> cobPerformers = availableEmployees.stream()
                .filter(e -> e.getType() == EmployeeType.COB_PERFORMER)
                .collect(Collectors.toList());
        
        return cobPerformers.size() >= desiredCount ? cobPerformers.subList(0, desiredCount) : cobPerformers;
    }

    private List<Employee> pickFairCobEmployeesForSaturday(List<Employee> availableEmployees, 
                                                        Map<LocalDate, List<Employee>> saturdayCobAssignments, 
                                                        LocalDate saturdayDate) {
    // Initialize if not already tracked
    saturdayCobAssignments.putIfAbsent(saturdayDate, new ArrayList<>());
    
    // Round-robin approach: get employees who haven't been assigned recently
    List<Employee> previousSaturdays = saturdayCobAssignments.get(saturdayDate);
    
    // Prefer employees who haven't been assigned recently
    List<Employee> availableForCob = availableEmployees.stream()
            .filter(e -> !previousSaturdays.contains(e))  // Avoid employees who worked the previous Saturday
            .collect(Collectors.toList());

    // Pick 3 employees for the Saturday COB shift
    List<Employee> selectedCobEmployees = pickEmployeesForCob(availableForCob, 3);
    
    // Update the tracker for future Saturday assignments
    saturdayCobAssignments.get(saturdayDate).addAll(selectedCobEmployees);

    return selectedCobEmployees;
}
    // Pick employees for Support (2 juniors)
    private List<Employee> pickEmployeesForSupport(List<Employee> availableEmployees, int desiredCount) {
        List<Employee> juniors = availableEmployees.stream()
                .filter(e -> e.getLevel() == EmployeeLevel.JUNIOR)
                .collect(Collectors.toList());
        
        return juniors.size() >= desiredCount ? juniors.subList(0, desiredCount) : juniors;
    }

    // Pick employees for System Monitor shift (1 employee)
    private List<Employee> pickEmployeesForSystemMonitor(List<Employee> availableEmployees, int desiredCount) {
        if (availableEmployees.isEmpty()) return new ArrayList<>();
        
        return availableEmployees.subList(0, Math.min(availableEmployees.size(), desiredCount));
    }

    // Assign the shift to the employee
    private void assignShift(LocalDate date, Employee employee, ShiftType shiftType) {
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

        supportScheduleRepository.save(schedule); // Save the new or updated schedule
    }

    // Assign leave after COB shift
    private void assignLeaveAfterCob(Employee employee, LocalDate leaveDate) {
        Leave leave = new Leave();
        leave.setEmployee(employee);
        employee.setStatus(EmployeeStatus.ON_LEAVE);
        leave.setStartDate(leaveDate);
        leave.setEndDate(leaveDate);
        leave.setStatus(LeaveStatus.APPROVED);
        leave.setLeaveReason("Rest after COB shift");
        leaveRepository.save(leave);
    }
}
