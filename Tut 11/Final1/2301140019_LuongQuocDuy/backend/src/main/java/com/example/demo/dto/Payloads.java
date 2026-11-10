package com.example.demo.dto;

import jakarta.validation.constraints.*;
import java.util.Set;

public final class Payloads {

  private Payloads() {}

  public record StudentInput(@NotBlank String name, @Min(18) int age) {}

  public record CompanyInput(
    @NotBlank @Size(min = 3, max = 30) String name,
    @NotBlank String image,
    @NotBlank @Size(min = 5, max = 58) String address
  ) {}

  public record JobInput(
    @NotBlank String title,
    @Size(max = 2000) String description
  ) {}

  public record EmployeeInput(
    @NotBlank @Size(min = 3, max = 30) String name,
    @Min(18) @Max(55) int age,
    @NotBlank String image,
    @NotBlank String address,
    @NotBlank @Email String email,
    Long companyId,
    Set<Long> jobIds
  ) {}

  public record LoginInput(
    @NotBlank String username,
    @NotBlank String password
  ) {}

  public record RegisterInput(
    @Pattern(
      regexp = "[a-zA-Z0-9_]{3,30}",
      message = "Use 3-30 letters, numbers or underscores"
    )
    @NotBlank
    String username,
    @Size(min = 8, max = 72) @NotBlank String password
  ) {}
}
