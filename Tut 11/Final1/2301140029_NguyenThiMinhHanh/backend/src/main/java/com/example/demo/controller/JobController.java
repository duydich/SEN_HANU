package com.example.demo.controller;

import com.example.demo.dto.Payloads.JobInput;
import com.example.demo.dto.Views;
import com.example.demo.entity.Job;
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
@RequestMapping("/api/jobs")
@Transactional
public class JobController {

  private final JobRepository repo;
  private final EmployeeRepository employees;

  public JobController(JobRepository repo, EmployeeRepository employees) {
    this.repo = repo;
    this.employees = employees;
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
      !List.of("id", "title").contains(sort) ||
      !List.of("asc", "desc").contains(direction)
    ) throw new ResponseStatusException(
      HttpStatus.BAD_REQUEST,
      "Invalid page or sort parameters."
    );
    Specification<Job> spec = (root, q, cb) ->
      cb.like(
        cb.lower(root.get("title")),
        "%" + search.toLowerCase(Locale.ROOT) + "%"
      );
    Sort ordering = Sort.by(Sort.Direction.fromString(direction), sort);
    if (!sort.equals("id")) ordering = ordering.and(Sort.by("id"));
    var result = repo.findAll(spec, PageRequest.of(page, size, ordering));
    return Map.of(
      "content",
      result.getContent().stream().map(Views::job).toList(),
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
    return Views.job(find(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> add(@Valid @RequestBody JobInput input) {
    var e = new Job();
    apply(e, input);
    return Views.job(repo.saveAndFlush(e));
  }

  @PutMapping("/{id}")
  public Map<String, Object> update(
    @PathVariable Long id,
    @Valid @RequestBody JobInput input
  ) {
    var e = find(id);
    apply(e, input);
    return Views.job(repo.saveAndFlush(e));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    var e = find(id);
    if (employees.existsByJobsId(id)) throw new ResponseStatusException(
      HttpStatus.CONFLICT,
      "Remove this job from employees before deleting it."
    );
    repo.delete(e);
    repo.flush();
  }

  private Job find(Long id) {
    return repo
      .findById(id)
      .orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found.")
      );
  }

  private void apply(Job e, JobInput i) {
    e.setTitle(i.title());
    e.setDescription(i.description());
  }
}
