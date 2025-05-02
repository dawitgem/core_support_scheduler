package com.wso2test.demo.model;

import lombok.Data;

@Data
public class EmployeeRequest {
   private Long id;
   private String name;
   private String  email;
   private String phoneNumber;
   private EmployeeStatus status;
   private EmployeeType type;
   private EmployeeLevel level;

}
