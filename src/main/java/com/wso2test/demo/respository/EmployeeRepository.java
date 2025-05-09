package com.wso2test.demo.respository;

import com.wso2test.demo.model.Employee;
import com.wso2test.demo.model.EmployeeLevel;
import com.wso2test.demo.model.EmployeeStatus;
import com.wso2test.demo.model.EmployeeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByPhoneNumber(String phoneNumber);

    List<Employee> findByStatus(EmployeeStatus status);

    List<Employee> findByNameContainingIgnoreCase(String name);

    List<Employee> findByType(EmployeeType type);

    List<Employee> findByTypeAndLevel(EmployeeType type, EmployeeLevel level);

    List<Employee> findByStatusAndType(EmployeeStatus status, EmployeeType type);

    List<Employee> findByStatusAndTypeAndLevel(EmployeeStatus status, EmployeeType type, EmployeeLevel level);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    @Query("SELECT e FROM Employee e WHERE e.id NOT IN (SELECT l.employee.id FROM Leave l WHERE :date BETWEEN l.startDate AND l.endDate)")
       List<Employee> findAvailableEmployeesOnDate(@Param("date") LocalDate date);


       @Query("SELECT e FROM Employee e WHERE e.status = com.wso2test.demo.model.EmployeeStatus.ACTIVE")
List<Employee> findAllActiveEmployees();

@Query("SELECT e FROM Employee e WHERE e.status = com.wso2test.demo.model.EmployeeStatus.ACTIVE AND e.type = com.wso2test.demo.model.EmployeeType.SUPPORT")
List<Employee> findAllActiveSupportEmployees();

}
