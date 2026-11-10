package com.example.demo.config;

import com.example.demo.repository.AppUserRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.*;
import org.springframework.web.cors.*;

@Configuration
public class SecurityConfig {

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  UserDetailsService userDetailsService(AppUserRepository users) {
    return username -> {
      var u = users
        .findByUsername(username)
        .orElseThrow(() ->
          new UsernameNotFoundException("Invalid credentials")
        );
      return User.withUsername(u.getUsername())
        .password(u.getPassword())
        .roles(u.getRole())
        .build();
    };
  }

  @Bean
  AuthenticationManager authenticationManager(
    UserDetailsService users,
    PasswordEncoder encoder
  ) {
    var provider = new DaoAuthenticationProvider(users);
    provider.setPasswordEncoder(encoder);
    return new ProviderManager(provider);
  }

  @Bean
  HttpSessionSecurityContextRepository contextRepository() {
    return new HttpSessionSecurityContextRepository();
  }

  @Bean
  CorsConfigurationSource cors(@Value("${app.cors-origins}") String origins) {
    var c = new CorsConfiguration();
    c.setAllowedOrigins(List.of(origins.split(",")));
    c.setAllowCredentials(true);
    c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    c.setAllowedHeaders(List.of("Content-Type", "X-CSRF-TOKEN"));
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", c);
    return source;
  }

  @Bean
  SecurityFilterChain security(
    HttpSecurity http,
    HttpSessionSecurityContextRepository repo,
    @org.springframework.beans.factory.annotation.Qualifier(
      "cors"
    ) CorsConfigurationSource cors
  ) throws Exception {
    return http
      .cors(c -> c.configurationSource(cors))
      .securityContext(c -> c.securityContextRepository(repo))
      .csrf(c -> c.csrfTokenRepository(new HttpSessionCsrfTokenRepository()))
      .authorizeHttpRequests(a ->
        a
          .requestMatchers("/api/auth/**", "/error")
          .permitAll()
          .requestMatchers(HttpMethod.OPTIONS, "/**")
          .permitAll()
          .requestMatchers(HttpMethod.GET, "/**")
          .authenticated()
          .anyRequest()
          .hasRole("ADMIN")
      )
      .exceptionHandling(e ->
        e
          .authenticationEntryPoint((req, res, ex) -> {
            res.setStatus(401);
            res.setContentType("application/json");
            res.getWriter().write("{\"message\":\"Please sign in.\"}");
          })
          .accessDeniedHandler((req, res, ex) -> {
            res.setStatus(403);
            res.setContentType("application/json");
            res
              .getWriter()
              .write(
                "{\"message\":\"Permission denied or invalid CSRF token. Reload and sign in again.\"}"
              );
          })
      )
      .build();
  }
}
