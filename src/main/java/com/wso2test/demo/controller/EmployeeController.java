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
import java.util.Optional;

@RestController
@RequestMapping("/api/employees") 
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeController(EmployeeService employeeService,EmployeeRepository employeeRepository) {
        this.employeeService = employeeService;
        this.employeeRepository=employeeRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
        Employee savedEmployee = employeeService.addEmployee(employee);
        return ResponseEntity.ok(savedEmployee);
    }

    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        System.out.println(employees);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/schedule")
    public ResponseEntity<List<Employee>> getEmployeesSchedule() {
        List<Employee> employees = employeeRepository.findAll();
        System.out.println(employees);
        return ResponseEntity.ok(employees);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        Optional<Employee> employee = employeeRepository.findById(id);
        if (employee.isPresent()) {
            return ResponseEntity.ok(employee.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @RequestBody EmployeeRequest employeeDetails) {
        
        Employee updatedEmployee = employeeService.updateEmployee(id, employeeDetails);
        return ResponseEntity.ok(updatedEmployee);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deletEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteEmployees() {
        employeeRepository.deleteAll();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Employee>> searchEmployeesByName(@RequestParam("name") String name) {
        List<Employee> employees = employeeRepository.findByNameContainingIgnoreCase(name);
        return ResponseEntity.ok(employees);
    }

   @GetMapping("/status/{status}")
    public ResponseEntity<List<Employee>> getEmployeesByStatus(@PathVariable EmployeeStatus status) {
        List<Employee> employees = employeeRepository.findByStatus(status);
        return ResponseEntity.ok(employees);
    }
}
