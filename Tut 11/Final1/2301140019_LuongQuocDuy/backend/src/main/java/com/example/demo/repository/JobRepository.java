package com.example.demo.repository;

import com.example.demo.entity.Job;
import org.springframework.data.jpa.repository.*;

public interface JobRepository
  extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {}
