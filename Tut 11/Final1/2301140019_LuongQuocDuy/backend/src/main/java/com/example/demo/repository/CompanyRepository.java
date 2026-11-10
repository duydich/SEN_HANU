package com.example.demo.repository;

import com.example.demo.entity.Company;
import org.springframework.data.jpa.repository.*;

public interface CompanyRepository
  extends JpaRepository<Company, Long>, JpaSpecificationExecutor<Company> {}
