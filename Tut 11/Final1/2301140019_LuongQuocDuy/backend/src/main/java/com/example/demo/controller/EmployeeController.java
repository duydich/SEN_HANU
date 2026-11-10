package com.example.demo.controller;

import com.example.demo.dto.Payloads.EmployeeInput;
import com.example.demo.dto.Views;
import com.example.demo.entity.Employee;
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
@RequestMapping("/api/employees")
@Transactional
public class EmployeeController {

  private final EmployeeRepository repo;
  private final CompanyRepository companies;
  private final JobRepository jobs;

  public EmployeeController(
    EmployeeRepository repo,
    CompanyRepository companies,
    JobRepository jobs
  ) {
    this.repo = repo;
    this.companies = companies;
    this.jobs = jobs;
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
    Specification<Employee> spec = (root, q, cb) ->
      cb.like(
        cb.lower(root.get("name")),
        "%" + search.toLowerCase(Locale.ROOT) + "%"
      );
    Sort ordering = Sort.by(Sort.Direction.fromString(direction), sort);
    if (!sort.equals("id")) ordering = ordering.and(Sort.by("id"));
    var result = repo.findAll(spec, PageRequest.of(page, size, ordering));
    return Map.of(
      "content",
      result.getContent().stream().map(Views::employee).toList(),
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
    return Views.employee(find(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> add(@Valid @RequestBody EmployeeInput input) {
    var e = new Employee();
    apply(e, input);
    return Views.employee(repo.saveAndFlush(e));
  }

  @PutMapping("/{id}")
  public Map<String, Object> update(
    @PathVariable Long id,
    @Valid @RequestBody EmployeeInput input
  ) {
    var e = find(id);
    apply(e, input);
    return Views.employee(repo.saveAndFlush(e));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    var e = find(id);
    repo.delete(e);
    repo.flush();
  }

  private Employee find(Long id) {
    return repo
      .findById(id)
      .orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found.")
      );
  }

  private void apply(Employee e, EmployeeInput i) {
    e.setName(i.name());
    e.setAge(i.age());
    e.setImage(i.image());
    e.setAddress(i.address());
    e.setEmail(i.email());
    e.setCompany(
      i.companyId() == null
        ? null
        : companies
            .findById(i.companyId())
            .orElseThrow(() ->
              new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Company does not exist."
              )
            )
    );
    var selected = new java.util.LinkedHashSet<com.example.demo.entity.Job>();
    if (i.jobIds() != null) for (Long id : i.jobIds())
      selected.add(
        jobs
          .findById(id)
          .orElseThrow(() ->
            new ResponseStatusException(
              HttpStatus.BAD_REQUEST,
              "Job does not exist."
            )
          )
      );
    e.setJobs(selected);
  }
}
