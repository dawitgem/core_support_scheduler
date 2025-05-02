package com.wso2test.demo.service;

import com.wso2test.demo.model.Employee;
import com.wso2test.demo.model.EmployeeRequest;
import com.wso2test.demo.model.EmployeeStatus;
import com.wso2test.demo.respository.EmployeeRepository;

import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    // --- CREATE ---
    public Employee addEmployee(Employee employee) {
        if (employeeRepository.findByEmail(employee.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use: " + employee.getEmail());
        }
        if (employeeRepository.findByPhoneNumber(employee.getPhoneNumber()).isPresent()) {
            throw new IllegalArgumentException("Phone number already in use: " + employee.getPhoneNumber());
        }
        return employeeRepository.save(employee);
    }

    // --- READ ---
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee with ID " + id + " not found"));
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public List<Employee> getEmployeesByStatus(EmployeeStatus status) {
        return employeeRepository.findByStatus(status);
    }

    public List<Employee> searchEmployeesByName(String name) {
        return employeeRepository.findByNameContainingIgnoreCase(name);
    }

    // --- UPDATE ---
    public Employee updateEmployee(Long id, EmployeeRequest details) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee with ID " + id + " not found"));

        if (details.getName() != null) existing.setName(details.getName());
        if (details.getEmail() != null) existing.setEmail(details.getEmail());
        if (details.getPhoneNumber() != null) existing.setPhoneNumber(details.getPhoneNumber());
        if (details.getStatus() != null) existing.setStatus(details.getStatus());

        return employeeRepository.save(existing);
    }

    // --- DELETE ---
    public void deleteEmployee(Long id) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee with ID " + id + " not found"));
        employeeRepository.delete(existing);
    }

    // --- OPTIONAL: Useful during shift generation ---
    public boolean isActiveEmployee(Long id) {
        return employeeRepository.findById(id)
                .map(emp -> emp.getStatus() == EmployeeStatus.ACTIVE)
                .orElse(false);
    }
}
