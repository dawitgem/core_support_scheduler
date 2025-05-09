package com.wso2test.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class DailySupportScheduleRow {
    private LocalDate date;
    private String dayOfWeek;
    private List<String> reservedStaff = new ArrayList<>(); // Names (e.g., could be the senior for the previous/next day) - Placeholder, logic needs definition
    private List<String> assignedGroupA = new ArrayList<>(); // Names of employees (1 Senior, 2 Juniors)
    private String remark; // For holidays or special notes

    public DailySupportScheduleRow(LocalDate date, String dayOfWeek) {
        this.date = date;
        this.dayOfWeek = dayOfWeek;
    }

    public String getAssignedGroupADisplay() {
        return String.join(" & ", assignedGroupA);
    }

    public String getReservedStaffDisplay() {
        return String.join(" & ", reservedStaff);
    }
}