package com.example.demo.repository;

import com.example.demo.entity.Employee;
import org.springframework.data.jpa.repository.*;

public interface EmployeeRepository
  extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee>
{
  boolean existsByCompanyId(Long id);
  boolean existsByJobsId(Long id);
}
