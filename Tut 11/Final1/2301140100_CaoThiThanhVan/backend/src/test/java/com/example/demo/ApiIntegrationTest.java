package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;

import com.example.demo.entity.AppUser;
import com.example.demo.repository.*;
import com.jayway.jsonpath.JsonPath;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Real HTTP requests through the complete security, validation and persistence stack. */
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = {
    "spring.datasource.url=jdbc:h2:mem:tut11;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "app.seed-enabled=false",
    "app.upload-dir=target/test-uploads",
  }
)
class ApiIntegrationTest {

  @LocalServerPort
  int port;

  @Autowired
  StudentRepository students;

  @Autowired
  EmployeeRepository employees;

  @Autowired
  CompanyRepository companies;

  @Autowired
  JobRepository jobs;

  @Autowired
  AppUserRepository users;

  @Autowired
  PasswordEncoder encoder;

  @BeforeEach
  void reset() {
    employees.deleteAll();
    companies.deleteAll();
    jobs.deleteAll();
    students.deleteAll();
    users.deleteAll();
    account("admin", "ADMIN");
    account("user", "USER");
  }

  void account(String username, String role) {
    var u = new AppUser();
    u.setUsername(username);
    u.setPassword(encoder.encode("Password11!"));
    u.setRole(role);
    users.save(u);
  }

  class Client {

    HttpClient http = HttpClient.newBuilder()
      .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
      .build();
    String token;

    HttpResponse<String> call(String method, String path, String body)
      throws Exception {
      var b = HttpRequest.newBuilder(
        URI.create("http://127.0.0.1:" + port + path)
      ).header("Content-Type", "application/json");
      if (token != null) b.header("X-CSRF-TOKEN", token);
      return http.send(
        b
          .method(
            method,
            body == null
              ? HttpRequest.BodyPublishers.noBody()
              : HttpRequest.BodyPublishers.ofString(body)
          )
          .build(),
        HttpResponse.BodyHandlers.ofString()
      );
    }

    void csrf() throws Exception {
      token = JsonPath.read(
        call("GET", "/api/auth/csrf", null).body(),
        "$.token"
      );
    }

    Client login(String username) throws Exception {
      csrf();
      assertEquals(
        200,
        call(
          "POST",
          "/api/auth/login",
          "{\"username\":\"" + username + "\",\"password\":\"Password11!\"}"
        ).statusCode()
      );
      csrf();
      return this;
    }

    long create(String kind, String json) throws Exception {
      var r = call("POST", "/api/" + kind, json);
      assertEquals(201, r.statusCode(), r.body());
      return ((Number) JsonPath.read(r.body(), "$.id")).longValue();
    }
  }

  @Test
  void studentCrudAndDetail() throws Exception {
    var c = new Client().login("admin");
    long id = c.create("students", "{\"name\":\"First Student\",\"age\":20}");
    assertEquals(
      "First Student",
      JsonPath.read(c.call("GET", "/api/students/" + id, null).body(), "$.name")
    );
    assertEquals(
      200,
      c
        .call(
          "PUT",
          "/api/students/" + id,
          "{\"name\":\"Updated Student\",\"age\":21}"
        )
        .statusCode()
    );
    assertEquals(
      21,
      (int) JsonPath.read(
        c.call("GET", "/api/students/" + id, null).body(),
        "$.age"
      )
    );
    assertEquals(
      204,
      c.call("DELETE", "/api/students/" + id, null).statusCode()
    );
    assertEquals(404, c.call("GET", "/api/students/" + id, null).statusCode());
  }

  @Test
  void validationAndMissingRecords() throws Exception {
    var c = new Client().login("admin");
    var r = c.call("POST", "/api/students", "{\"name\":\"\",\"age\":12}");
    assertEquals(400, r.statusCode());
    assertNotNull(JsonPath.read(r.body(), "$.errors.name"));
    assertNotNull(JsonPath.read(r.body(), "$.errors.age"));
    assertEquals(
      404,
      c
        .call("PUT", "/api/students/999999", "{\"name\":\"Nobody\",\"age\":21}")
        .statusCode()
    );
    assertEquals(400, c.call("POST", "/api/students", "not-json").statusCode());
    assertEquals(
      400,
      c.call("GET", "/api/students?page=-1", null).statusCode()
    );
    assertEquals(
      400,
      c.call("GET", "/api/students?sort=password", null).statusCode()
    );
  }

  @Test
  void searchSortAndPagination() throws Exception {
    var c = new Client().login("admin");
    for (String name : List.of("Charlie", "Alice", "Bob"))
      c.create("students", "{\"name\":\"" + name + "\",\"age\":20}");
    var r = c.call("GET", "/api/students?size=2&sort=name&direction=asc", null);
    assertEquals(3, (int) JsonPath.read(r.body(), "$.totalElements"));
    assertEquals(2, (int) JsonPath.read(r.body(), "$.totalPages"));
    assertEquals("Alice", JsonPath.read(r.body(), "$.content[0].name"));
    assertEquals(
      "Charlie",
      JsonPath.read(
        c.call("GET", "/api/students?size=2&page=1&sort=name", null).body(),
        "$.content[0].name"
      )
    );
    assertEquals(
      "Charlie",
      JsonPath.read(
        c.call("GET", "/api/students?search=CHAR", null).body(),
        "$.content[0].name"
      )
    );
  }

  @Test
  void relationsAndDeletionConflicts() throws Exception {
    var c = new Client().login("admin");
    long company = c.create(
      "companies",
      "{\"name\":\"Test Company\",\"address\":\"Hanoi city\",\"image\":\"/assets/company.svg\"}"
    );
    long job = c.create(
      "jobs",
      "{\"title\":\"Developer\",\"description\":\"Build products\"}"
    );
    String body =
      "{\"name\":\"Test Employee\",\"age\":25,\"email\":\"test@example.com\",\"address\":\"Hanoi\",\"image\":\"/assets/person.svg\",\"companyId\":" +
      company +
      ",\"jobIds\":[" +
      job +
      "]}";
    long emp = c.create("employees", body);
    var response = c.call("GET", "/api/employees/" + emp, null);
    assertEquals(
      "Test Company",
      JsonPath.read(response.body(), "$.company.name")
    );
    assertEquals(
      "Developer",
      JsonPath.read(response.body(), "$.jobs[0].title")
    );
    assertEquals(
      409,
      c.call("DELETE", "/api/companies/" + company, null).statusCode()
    );
    assertEquals(409, c.call("DELETE", "/api/jobs/" + job, null).statusCode());
    assertEquals(
      200,
      c
        .call(
          "PUT",
          "/api/employees/" + emp,
          body
            .replace("\"companyId\":" + company, "\"companyId\":null")
            .replace("\"jobIds\":[" + job + "]", "\"jobIds\":[]")
        )
        .statusCode()
    );
    assertEquals(
      204,
      c.call("DELETE", "/api/companies/" + company, null).statusCode()
    );
    assertEquals(204, c.call("DELETE", "/api/jobs/" + job, null).statusCode());
    assertEquals(
      204,
      c.call("DELETE", "/api/employees/" + emp, null).statusCode()
    );
  }

  @Test
  void companyAndJobUpdateValidation() throws Exception {
    var c = new Client().login("admin");
    long id = c.create(
      "companies",
      "{\"name\":\"Original Co\",\"address\":\"Hanoi city\",\"image\":\"/assets/company.svg\"}"
    );
    assertEquals(
      200,
      c
        .call(
          "PUT",
          "/api/companies/" + id,
          "{\"name\":\"Updated Co\",\"address\":\"Hanoi city\",\"image\":\"/assets/company.svg\"}"
        )
        .statusCode()
    );
    assertEquals(
      400,
      c
        .call(
          "POST",
          "/api/companies",
          "{\"name\":\"a\",\"address\":\"a\",\"image\":\"\"}"
        )
        .statusCode()
    );
    long job = c.create("jobs", "{\"title\":\"Designer\"}");
    assertEquals(
      200,
      c
        .call(
          "PUT",
          "/api/jobs/" + job,
          "{\"title\":\"Senior Designer\",\"description\":\"Updated\"}"
        )
        .statusCode()
    );
    assertEquals(
      400,
      c.call("POST", "/api/jobs", "{\"title\":\"\"}").statusCode()
    );
  }

  @Test
  void userCannotMutateAndAnonymousCannotRead() throws Exception {
    assertEquals(
      401,
      new Client().call("GET", "/api/students", null).statusCode()
    );
    var c = new Client().login("user");
    assertEquals(200, c.call("GET", "/api/students", null).statusCode());
    assertEquals(
      403,
      c
        .call("POST", "/api/students", "{\"name\":\"Test\",\"age\":20}")
        .statusCode()
    );
    var admin = new Client().login("admin");
    admin.token = null;
    assertEquals(
      403,
      admin.call("POST", "/api/jobs", "{\"title\":\"Test\"}").statusCode()
    );
  }

  @Test
  void registrationSessionAndLogout() throws Exception {
    var c = new Client();
    c.csrf();
    assertEquals(
      201,
      c
        .call(
          "POST",
          "/api/auth/register",
          "{\"username\":\"newuser\",\"password\":\"Password11!\"}"
        )
        .statusCode()
    );
    assertEquals(
      "USER",
      users.findByUsername("newuser").orElseThrow().getRole()
    );
    assertEquals(
      409,
      c
        .call(
          "POST",
          "/api/auth/register",
          "{\"username\":\"newuser\",\"password\":\"Password11!\"}"
        )
        .statusCode()
    );
    c.login("newuser");
    assertEquals(
      "USER",
      JsonPath.read(c.call("GET", "/api/auth/me", null).body(), "$.role")
    );
    assertEquals(200, c.call("POST", "/api/auth/logout", null).statusCode());
    assertEquals(401, c.call("GET", "/api/students", null).statusCode());
  }

  @Test
  void legacyStudentPathsRemainAvailable() throws Exception {
    var c = new Client().login("admin");
    var r = c.call("POST", "/add", "{\"name\":\"Legacy Student\",\"age\":20}");
    assertEquals(201, r.statusCode());
    long id = ((Number) JsonPath.read(r.body(), "$.id")).longValue();
    assertEquals(200, c.call("GET", "/", null).statusCode());
    assertEquals(200, c.call("GET", "/detail/" + id, null).statusCode());
    assertEquals(
      200,
      c
        .call(
          "PUT",
          "/update/" + id,
          "{\"name\":\"Legacy Updated\",\"age\":23}"
        )
        .statusCode()
    );
    assertEquals(204, c.call("DELETE", "/delete/" + id, null).statusCode());
  }

  @Test
  void corsPreflightUsesConfiguredOrigin() throws Exception {
    var c = new Client();
    var request = HttpRequest.newBuilder(
      URI.create("http://127.0.0.1:" + port + "/api/students")
    )
      .header("Origin", "http://localhost:3000")
      .header("Access-Control-Request-Method", "PUT")
      .header("Access-Control-Request-Headers", "Content-Type,X-CSRF-TOKEN")
      .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
      .build();
    var r = c.http.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(200, r.statusCode());
    assertEquals(
      "http://localhost:3000",
      r.headers().firstValue("Access-Control-Allow-Origin").orElseThrow()
    );
  }

  @Test
  void uploadRejectsNonImagesAndServesRealImage() throws Exception {
    var c = new Client().login("admin");
    for (boolean valid : List.of(false, true)) {
      byte[] file = valid
        ? Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jRZkAAAAASUVORK5CYII="
          )
        : "not an image".getBytes(StandardCharsets.UTF_8);
      String boundary = "Tut11Boundary";
      byte[] begin = (
        "--" +
        boundary +
        "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"test.png\"\r\nContent-Type: " +
        (valid ? "image/png" : "text/plain") +
        "\r\n\r\n"
      ).getBytes(StandardCharsets.UTF_8);
      byte[] end = ("\r\n--" + boundary + "--\r\n").getBytes(
        StandardCharsets.UTF_8
      );
      byte[] body = new byte[begin.length + file.length + end.length];
      System.arraycopy(begin, 0, body, 0, begin.length);
      System.arraycopy(file, 0, body, begin.length, file.length);
      System.arraycopy(end, 0, body, begin.length + file.length, end.length);
      var req = HttpRequest.newBuilder(
        URI.create("http://127.0.0.1:" + port + "/api/uploads")
      )
        .header("X-CSRF-TOKEN", c.token)
        .header("Content-Type", "multipart/form-data; boundary=" + boundary)
        .POST(HttpRequest.BodyPublishers.ofByteArray(body))
        .build();
      var r = c.http.send(req, HttpResponse.BodyHandlers.ofString());
      assertEquals(valid ? 201 : 400, r.statusCode(), r.body());
      if (valid) {
        String path = JsonPath.read(r.body(), "$.url");
        assertEquals(200, c.call("GET", path, null).statusCode());
      }
    }
  }
}
