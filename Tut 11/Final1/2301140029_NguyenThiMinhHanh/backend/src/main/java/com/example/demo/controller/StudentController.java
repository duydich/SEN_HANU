package com.example.demo.controller;

import com.example.demo.dto.Payloads.StudentInput;
import com.example.demo.dto.Views;
import com.example.demo.entity.Student;
import com.example.demo.repository.*;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/students")
@Transactional
public class StudentController {

  private final StudentRepository repo;

  public StudentController(StudentRepository repo) {
    this.repo = repo;
  }

  @GetMapping
  public Map<String, Object> list(
    @RequestParam(defaultValue = "") String search,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "5") int size,
    @RequestParam(defaultValue = "id") String sort,
    @RequestParam(defaultValue = "asc") String direction
  ) {
    if (
      page < 0 ||
      size < 1 ||
      size > 100 ||
      !List.of("id", "name", "age").contains(sort) ||
      !List.of("asc", "desc").contains(direction)
    ) throw new ResponseStatusException(
      HttpStatus.BAD_REQUEST,
      "Invalid page or sort parameters."
    );
    Specification<Student> spec = (root, q, cb) ->
      cb.like(
        cb.lower(root.get("name")),
        "%" + search.toLowerCase(Locale.ROOT) + "%"
      );
    Sort ordering = Sort.by(Sort.Direction.fromString(direction), sort);
    if (!sort.equals("id")) ordering = ordering.and(Sort.by("id"));
    var result = repo.findAll(spec, PageRequest.of(page, size, ordering));
    return Map.of(
      "content",
      result.getContent().stream().map(Views::student).toList(),
      "page",
      page,
      "size",
      size,
      "totalElements",
      result.getTotalElements(),
      "totalPages",
      result.getTotalPages()
    );
  }

  @GetMapping("/{id}")
  public Map<String, Object> detail(@PathVariable Long id) {
    return Views.student(find(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> add(@Valid @RequestBody StudentInput input) {
    var e = new Student();
    apply(e, input);
    return Views.student(repo.saveAndFlush(e));
  }

  @PutMapping("/{id}")
  public Map<String, Object> update(
    @PathVariable Long id,
    @Valid @RequestBody StudentInput input
  ) {
    var e = find(id);
    apply(e, input);
    return Views.student(repo.saveAndFlush(e));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    var e = find(id);
    repo.delete(e);
    repo.flush();
  }

  private Student find(Long id) {
    return repo
      .findById(id)
      .orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found.")
      );
  }

  private void apply(Student e, StudentInput i) {
    e.setName(i.name());
    e.setAge(i.age());
  }
}
