package com.example.demo.dto;

import com.example.demo.entity.*;
import java.util.*;

public final class Views {

  private Views() {}

  public static Map<String, Object> student(Student s) {
    return Map.of("id", s.getId(), "name", s.getName(), "age", s.getAge());
  }

  public static Map<String, Object> company(Company c) {
    return Map.of(
      "id",
      c.getId(),
      "name",
      c.getName(),
      "image",
      c.getImage(),
      "address",
      c.getAddress()
    );
  }

  public static Map<String, Object> job(Job j) {
    return Map.of(
      "id",
      j.getId(),
      "title",
      j.getTitle(),
      "description",
      Objects.toString(j.getDescription(), "")
    );
  }

  public static Map<String, Object> employee(Employee e) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", e.getId());
    m.put("name", e.getName());
    m.put("age", e.getAge());
    m.put("image", e.getImage());
    m.put("address", e.getAddress());
    m.put("email", e.getEmail());
    m.put("company", e.getCompany() == null ? null : company(e.getCompany()));
    m.put(
      "jobs",
      e
        .getJobs()
        .stream()
        .sorted(Comparator.comparing(Job::getId))
        .map(Views::job)
        .toList()
    );
    return m;
  }
}
