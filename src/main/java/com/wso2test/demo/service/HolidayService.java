package com.wso2test.demo.service;

import com.wso2test.demo.model.Holiday;
import com.wso2test.demo.respository.HolidayRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class HolidayService {

    @Autowired
    private HolidayRepository holidayRepository;

    // Add a new holiday
    public Holiday addHoliday(Holiday holiday) {
        // Check if the holiday already exists
        if (holidayRepository.existsByDate(holiday.getDate())) {
            throw new IllegalArgumentException("Holiday already exists for this date.");
        }
        return holidayRepository.save(holiday);
    }

    // Get all holidays
    public List<Holiday> getAllHolidays() {
        return holidayRepository.findAll();
    }

    // Get holidays by a specific date
    public List<Holiday> getHolidaysByDate(LocalDate date) {
        return holidayRepository.findByDate(date);
    }
}
