package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "company")
public class Company {

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

  @NotBlank
  private String image;

  @NotBlank
  @Size(min = 5, max = 58)
  private String address;

  public String getName() {
    return name;
  }

  public void setName(String value) {
    this.name = value;
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
}
