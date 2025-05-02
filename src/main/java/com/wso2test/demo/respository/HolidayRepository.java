package com.wso2test.demo.respository;

import com.wso2test.demo.model.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    // Find all holidays
    List<Holiday> findAll();

    // Find holidays by date (if needed)
    List<Holiday> findByDate(LocalDate date);

    // Check if a specific holiday exists for a given date
    boolean existsByDate(LocalDate date);
}
