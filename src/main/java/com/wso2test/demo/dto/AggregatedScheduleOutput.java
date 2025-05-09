package com.wso2test.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AggregatedScheduleOutput {
    private List<DailyNonSupportScheduleRow> nonSupportSchedule;
    private List<DailySupportScheduleRow> supportSchedule;
}