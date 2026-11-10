package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "employee")
public class Employee {

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
  @Size(min = 3, max = 30)
  private String name;

  @Min(18)
  @Max(55)
  private int age;

  @NotBlank
  private String image;

  @NotBlank
  private String address;

  @NotBlank
  @Email
  private String email;

  @ManyToOne(fetch = FetchType.EAGER)
  private Company company;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
    name = "employee_job",
    joinColumns = @JoinColumn(name = "employee_id"),
    inverseJoinColumns = @JoinColumn(name = "job_id")
  )
  private java.util.Set<Job> jobs = new java.util.LinkedHashSet<>();

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

  public String getImage() {
    return image;
  }

  public void setImage(String value) {
    this.image = value;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String value) {
    this.address = value;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String value) {
    this.email = value;
  }

  public Company getCompany() {
    return company;
  }

  public void setCompany(Company value) {
    this.company = value;
  }

  public java.util.Set<Job> getJobs() {
    return jobs;
  }

  public void setJobs(java.util.Set<Job> value) {
    this.jobs = value;
  }
}
