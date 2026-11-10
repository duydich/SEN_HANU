package com.example.demo.controller;

import com.example.demo.dto.Payloads.CompanyInput;
import com.example.demo.dto.Views;
import com.example.demo.entity.Company;
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
@RequestMapping("/api/companies")
@Transactional
public class CompanyController {

  private final CompanyRepository repo;
  private final EmployeeRepository employees;

  public CompanyController(
    CompanyRepository repo,
    EmployeeRepository employees
  ) {
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
      !List.of("id", "name").contains(sort) ||
      !List.of("asc", "desc").contains(direction)
    ) throw new ResponseStatusException(
      HttpStatus.BAD_REQUEST,
      "Invalid page or sort parameters."
    );
    Specification<Company> spec = (root, q, cb) ->
      cb.like(
        cb.lower(root.get("name")),
        "%" + search.toLowerCase(Locale.ROOT) + "%"
      );
    Sort ordering = Sort.by(Sort.Direction.fromString(direction), sort);
    if (!sort.equals("id")) ordering = ordering.and(Sort.by("id"));
    var result = repo.findAll(spec, PageRequest.of(page, size, ordering));
    return Map.of(
      "content",
      result.getContent().stream().map(Views::company).toList(),
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
    return Views.company(find(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> add(@Valid @RequestBody CompanyInput input) {
    var e = new Company();
    apply(e, input);
    return Views.company(repo.saveAndFlush(e));
  }

  @PutMapping("/{id}")
  public Map<String, Object> update(
    @PathVariable Long id,
    @Valid @RequestBody CompanyInput input
  ) {
    var e = find(id);
    apply(e, input);
    return Views.company(repo.saveAndFlush(e));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    var e = find(id);
    if (employees.existsByCompanyId(id)) throw new ResponseStatusException(
      HttpStatus.CONFLICT,
      "Unassign employees from this company before deleting it."
    );
    repo.delete(e);
    repo.flush();
  }

  private Company find(Long id) {
    return repo
      .findById(id)
      .orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found.")
      );
  }

  private void apply(Company e, CompanyInput i) {
    e.setName(i.name());
    e.setImage(i.image());
    e.setAddress(i.address());
  }
}
