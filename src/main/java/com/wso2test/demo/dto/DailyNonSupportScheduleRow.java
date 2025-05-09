package com.wso2test.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class DailyNonSupportScheduleRow {
    private LocalDate date;
    private String dayOfWeek;
    private List<String> systemMonitoringPerformers = new ArrayList<>(); // Names of employees
    private List<String> eveningCobPerformers = new ArrayList<>();      // Names of employees
    private String remark; // For holidays or special notes

    public DailyNonSupportScheduleRow(LocalDate date, String dayOfWeek) {
        this.date = date;
        this.dayOfWeek = dayOfWeek;
    }

    public String getSystemMonitoringPerformersDisplay() {
        return String.join(" & ", systemMonitoringPerformers);
    }

    public String getEveningCobPerformersDisplay() {
        return String.join(" & ", eveningCobPerformers);
    }
}