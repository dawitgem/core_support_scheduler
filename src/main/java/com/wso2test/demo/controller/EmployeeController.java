package com.wso2test.demo.controller;

import com.wso2test.demo.model.Employee;
import com.wso2test.demo.model.EmployeeRequest;
import com.wso2test.demo.model.EmployeeStatus;
import com.wso2test.demo.respository.EmployeeRepository;
import com.wso2test.demo.service.EmployeeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeRepository employeeRepository;


    public EmployeeController(EmployeeService employeeService,EmployeeRepository employeeRepository) {
        this.employeeService = employeeService;
        this.employeeRepository=employeeRepository;
    }

  
    //--create multi--
    @PostMapping("/add")
    public ResponseEntity<List<Employee>> createMultipleEmployee(@RequestBody List<EmployeeRequest> employees) {
           List<Employee> newEmployees = employees.stream()
        .map(dto -> new Employee(dto.getName(), dto.getEmail(), dto.getPhoneNumber(), dto.getStatus(), dto.getType(), dto.getLevel()))
        .collect(Collectors.toList());

    List<Employee> saved = employeeRepository.saveAll(newEmployees);
    return ResponseEntity.ok(saved);
     
    }

    // --- READ ---
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Employee>> searchEmployeesByName(@RequestParam("name") String name) {
        return ResponseEntity.ok(employeeService.searchEmployeesByName(name));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Employee>> getEmployeesByStatus(@PathVariable EmployeeStatus status) {
        return ResponseEntity.ok(employeeService.getEmployeesByStatus(status));
    }

    // --- UPDATE ---
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(
            @PathVariable Long id,
            @RequestBody EmployeeRequest employeeDetails) {
        Employee updated = employeeService.updateEmployee(id, employeeDetails);
        return ResponseEntity.ok(updated);
    }

    // --- DELETE ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllEmployees() {
        // Optional: for safety, you might require a flag/confirmation token
        employeeService.getAllEmployees().forEach(e -> employeeService.deleteEmployee(e.getId()));
        return ResponseEntity.noContent().build();
    }
}
