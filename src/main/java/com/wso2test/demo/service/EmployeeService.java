package com.wso2test.demo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wso2test.demo.model.Employee;
import com.wso2test.demo.model.EmployeeRequest;
import com.wso2test.demo.model.EmployeeStatus;
import com.wso2test.demo.respository.EmployeeRepository;


@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

     @Autowired
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

       public Employee addEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    public Employee updateEmployee(Long employeeId,EmployeeRequest employeeDetail){
          Employee existingEmployee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new EntityNotFoundException("Employee with id " + employeeId + " not found"));

            if (employeeDetail.getName() != null) {
              existingEmployee.setName(employeeDetail.getName());
          }
          if (employeeDetail.getEmail() != null) {
              existingEmployee.setEmail(employeeDetail.getEmail());
          }
          if (employeeDetail.getStatus() != null) {
              existingEmployee.setStatus(employeeDetail.getStatus());
          }
          if (employeeDetail.getPhoneNumber() != null) {
              existingEmployee.setPhoneNumber(employeeDetail.getPhoneNumber());
          }
      
          return employeeRepository.save(existingEmployee);
        
    }

    public void   deletEmployee(Long employeeId){
        Employee existingEmployee = employeeRepository.findById(employeeId)
          .orElseThrow(() -> new EntityNotFoundException("Employee with id " + employeeId + " not found"));

           employeeRepository.delete(existingEmployee);
      
  }

 
  
    
 
}
