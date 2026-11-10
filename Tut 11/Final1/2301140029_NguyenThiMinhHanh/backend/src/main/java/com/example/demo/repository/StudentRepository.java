package com.example.demo.repository;

import com.example.demo.entity.Student;
import org.springframework.data.jpa.repository.*;

public interface StudentRepository
  extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {}
