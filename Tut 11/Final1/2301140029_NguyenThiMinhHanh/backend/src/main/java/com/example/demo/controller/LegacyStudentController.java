package com.example.demo.controller;

import com.example.demo.dto.Payloads.StudentInput;
import com.example.demo.dto.Views;
import com.example.demo.repository.StudentRepository;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** Original 2301140029 Tut10 paths retained alongside the paginated API. */
@RestController
public class LegacyStudentController {

  private final StudentController students;
  private final StudentRepository repo;

  public LegacyStudentController(
    StudentController students,
    StudentRepository repo
  ) {
    this.students = students;
    this.repo = repo;
  }

  @GetMapping("/")
  public List<Map<String, Object>> list() {
    return repo.findAll().stream().map(Views::student).toList();
  }

  @GetMapping("/detail/{id}")
  public Map<String, Object> detail(@PathVariable Long id) {
    return students.detail(id);
  }

  @PostMapping("/add")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> add(@Valid @RequestBody StudentInput i) {
    return students.add(i);
  }

  @PutMapping("/update/{id}")
  public Map<String, Object> update(
    @PathVariable Long id,
    @Valid @RequestBody StudentInput i
  ) {
    return students.update(id, i);
  }

  @DeleteMapping("/delete/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    students.delete(id);
  }
}
