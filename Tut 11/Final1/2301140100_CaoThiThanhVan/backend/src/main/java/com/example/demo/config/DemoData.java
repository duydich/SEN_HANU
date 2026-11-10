package com.example.demo.config;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DemoData implements CommandLineRunner {

  private final StudentRepository students;
  private final CompanyRepository companies;
  private final JobRepository jobs;
  private final EmployeeRepository employees;
  private final AppUserRepository users;
  private final PasswordEncoder encoder;

  @Value("${app.seed-enabled}")
  boolean enabled;

  @Value("${app.demo-admin-password}")
  String adminPassword;

  @Value("${app.demo-user-password}")
  String userPassword;

  public DemoData(
    StudentRepository s,
    CompanyRepository c,
    JobRepository j,
    EmployeeRepository e,
    AppUserRepository u,
    PasswordEncoder p
  ) {
    students = s;
    companies = c;
    jobs = j;
    employees = e;
    users = u;
    encoder = p;
  }

  @Override
  @Transactional
  public void run(String... args) {
    if (!enabled) return;
    account("admin", adminPassword, "ADMIN");
    account("user", userPassword, "USER");
    if (students.count() == 0) {
      String[] names = {
        "Cao Thị Thanh Vân",
        "An Nguyen",
        "Bao Tran",
        "Chi Le",
        "Duc Pham",
        "Giang Vu",
        "Hoa Dang",
        "Khanh Do",
        "Linh Bui",
        "Minh Hoang",
        "Phuong Ngo",
        "Trang Dinh",
      };
      for (int i = 0; i < names.length; i++) {
        var s = new Student();
        s.setName(names[i]);
        s.setAge(19 + ((i + 3) % 7));
        students.save(s);
      }
    }
    if (companies.count() == 0) {
      String[] names = {
        "Bloom Studio",
        "Velvet Technology",
        "Hanoi Atelier",
        "Meadow Labs",
        "Rosewood Creative",
        "Petal Software",
      };
      for (int i = 0; i < names.length; i++) {
        var c = new Company();
        c.setName(names[i]);
        c.setAddress(12 + i * 8 + " Nguyen Trai, Ha Noi");
        c.setImage("/assets/company.svg");
        companies.save(c);
      }
    }
    if (jobs.count() == 0) {
      for (String title : List.of(
        "Frontend Developer",
        "Backend Developer",
        "UI Designer",
        "QA Engineer",
        "Business Analyst",
        "Project Manager"
      )) {
        var j = new Job();
        j.setTitle(title);
        j.setDescription(
          "Work with the team as a " + title.toLowerCase() + "."
        );
        jobs.save(j);
      }
    }
    if (employees.count() == 0) {
      var cs = companies.findAll();
      var js = jobs.findAll();
      for (int i = 0; i < 12; i++) {
        var e = new Employee();
        e.setName(
          List.of(
            "Anh Nguyen",
            "Binh Tran",
            "Chau Le",
            "Dung Pham",
            "Ha Vu",
            "Hieu Dang",
            "Khanh Do",
            "Lan Bui",
            "Mai Hoang",
            "Nam Ngo",
            "Phuc Dinh",
            "Thao Vo"
          ).get(i)
        );
        e.setAge(22 + i + 3);
        e.setAddress(20 + i + " Cau Giay, Ha Noi");
        e.setEmail("employee" + (i + 1) + "@example.com");
        e.setImage("/assets/person.svg");
        e.setCompany(cs.get(i % cs.size()));
        e.setJobs(new LinkedHashSet<>(List.of(js.get(i % js.size()))));
        employees.save(e);
      }
    }
  }

  private void account(String username, String password, String role) {
    if (users.existsByUsername(username)) return;
    var u = new AppUser();
    u.setUsername(username);
    u.setPassword(encoder.encode(password));
    u.setRole(role);
    users.save(u);
  }
}
