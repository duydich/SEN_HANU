package com.example.demo.controller;

import java.util.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiErrors {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<?> invalid(MethodArgumentNotValidException e) {
    var fields = new LinkedHashMap<String, String>();
    e.getBindingResult()
      .getFieldErrors()
      .forEach(f -> fields.put(f.getField(), f.getDefaultMessage()));
    return ResponseEntity.badRequest().body(
      Map.of(
        "message",
        "Please check the highlighted fields.",
        "errors",
        fields
      )
    );
  }

  @ExceptionHandler(ResponseStatusException.class)
  ResponseEntity<?> status(ResponseStatusException e) {
    return ResponseEntity.status(e.getStatusCode()).body(
      Map.of("message", Objects.toString(e.getReason(), "Request failed."))
    );
  }

  @ExceptionHandler(AuthenticationException.class)
  ResponseEntity<?> auth(AuthenticationException e) {
    return ResponseEntity.status(401).body(
      Map.of("message", "Incorrect username or password.")
    );
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
  })
  ResponseEntity<?> bad(Exception e) {
    return ResponseEntity.badRequest().body(
      Map.of("message", "Invalid request data.")
    );
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<?> conflict(Exception e) {
    return ResponseEntity.status(409).body(
      Map.of(
        "message",
        "This record conflicts with existing data or is still in use."
      )
    );
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  ResponseEntity<?> large(Exception e) {
    return ResponseEntity.status(413).body(
      Map.of("message", "Image must be smaller than 5 MB.")
    );
  }
}
