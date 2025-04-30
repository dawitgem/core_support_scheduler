package com.wso2test.demo.model;

import lombok.Data;

@Data
public class LeaveRequest {
    private Long employeeId;
    private String leaveReason;
    private String startDate;  
    private String endDate;    

}