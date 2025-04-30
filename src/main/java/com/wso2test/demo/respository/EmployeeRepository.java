package com.wso2test.demo.respository;

import com.wso2test.demo.model.Employee;
import com.wso2test.demo.model.EmployeeStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByPhoneNumber(String phoneNumber);

    List<Employee> findByStatus(EmployeeStatus status);

    List<Employee> findByNameContainingIgnoreCase(String name);

    Optional<Employee> findById(Long id);
}
