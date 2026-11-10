package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "student")
public class Student {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  public Long getId() {
    return id;
  }

  public void setId(Long value) {
    id = value;
  }

  @NotBlank
  private String name;

  @Min(18)
  private int age;

  public String getName() {
    return name;
  }

  public void setName(String value) {
    this.name = value;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int value) {
    this.age = value;
  }
}
