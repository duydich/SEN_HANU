package com.example.demo.controller;

import com.example.demo.dto.Payloads.*;
import com.example.demo.entity.AppUser;
import com.example.demo.repository.AppUserRepository;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthenticationManager auth;
  private final HttpSessionSecurityContextRepository contexts;
  private final AppUserRepository users;
  private final PasswordEncoder encoder;

  public AuthController(
    AuthenticationManager a,
    HttpSessionSecurityContextRepository c,
    AppUserRepository u,
    PasswordEncoder e
  ) {
    auth = a;
    contexts = c;
    users = u;
    encoder = e;
  }

  @GetMapping("/csrf")
  public Map<String, String> csrf(CsrfToken token) {
    return Map.of("token", token.getToken());
  }

  @PostMapping("/login")
  public Map<String, Object> login(
    @Valid @RequestBody LoginInput input,
    HttpServletRequest req,
    HttpServletResponse res
  ) {
    Authentication result = auth.authenticate(
      UsernamePasswordAuthenticationToken.unauthenticated(
        input.username(),
        input.password()
      )
    );
    req.getSession();
    req.changeSessionId();
    var context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(result);
    SecurityContextHolder.setContext(context);
    contexts.saveContext(context, req, res);
    new HttpSessionCsrfTokenRepository().saveToken(null, req, res);
    return view(result);
  }

  @GetMapping("/me")
  public Map<String, Object> me(Authentication a) {
    if (
      a == null || a instanceof AnonymousAuthenticationToken
    ) throw new ResponseStatusException(
      HttpStatus.UNAUTHORIZED,
      "Please sign in."
    );
    return view(a);
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, String> register(@Valid @RequestBody RegisterInput i) {
    if (users.existsByUsername(i.username())) throw new ResponseStatusException(
      HttpStatus.CONFLICT,
      "Username already exists."
    );
    var u = new AppUser();
    u.setUsername(i.username());
    u.setPassword(encoder.encode(i.password()));
    u.setRole("USER");
    users.save(u);
    return Map.of("message", "Account created. Sign in to continue.");
  }

  @PostMapping("/logout")
  public Map<String, String> logout(HttpServletRequest req) {
    var s = req.getSession(false);
    if (s != null) s.invalidate();
    SecurityContextHolder.clearContext();
    return Map.of("message", "Signed out.");
  }

  private Map<String, Object> view(Authentication a) {
    return Map.of(
      "username",
      a.getName(),
      "role",
      a
        .getAuthorities()
        .stream()
        .anyMatch(x -> x.getAuthority().equals("ROLE_ADMIN"))
        ? "ADMIN"
        : "USER"
    );
  }
}
