package com.wso2test.demo.controller;

import com.wso2test.demo.model.Holiday;
import com.wso2test.demo.respository.HolidayRepository;
import com.wso2test.demo.service.HolidayService;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/holidays")
public class HolidayController {

    @Autowired
    private HolidayService holidayService;
    @Autowired
    private HolidayRepository holidayRepository;
    // Endpoint to add a new holiday
    @PostMapping
    public ResponseEntity<Holiday> addHoliday( @RequestBody Holiday holiday) {
        try {
            Holiday savedHoliday = holidayService.addHoliday(holiday);
            return new ResponseEntity<>(savedHoliday, HttpStatus.CREATED);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // Endpoint to get all holidays
    @GetMapping
    public ResponseEntity<?> getAllHolidays() {
        return new ResponseEntity<>(holidayService.getAllHolidays(), HttpStatus.OK);
    }

    // Endpoint to get holidays by specific date
    @GetMapping("/date/{date}")
    public ResponseEntity<?> getHolidaysByDate(@PathVariable("date") String date) {
        LocalDate localDate = LocalDate.parse(date);
        return new ResponseEntity<>(holidayService.getHolidaysByDate(localDate), HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllSchedules() {
        holidayRepository.deleteAll(); // This deletes all SupportSchedule entities
        // If you also need to reset other data (like auto-generated leaves), do it here.
        // e.g., leaveRepository.deleteByReason("Rest after COB shift");
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}