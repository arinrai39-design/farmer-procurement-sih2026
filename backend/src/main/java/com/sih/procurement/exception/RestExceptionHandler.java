package com.sih.procurement.exception;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestControllerAdvice
public class RestExceptionHandler {
  @ExceptionHandler(ApiException.class)
  ResponseEntity<Map<String, Object>> api(ApiException ex) {
    return ResponseEntity.status(ex.status()).body(error(ex.status(), ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<Map<String, Object>> validation() {
    return ResponseEntity.badRequest().body(error(HttpStatus.BAD_REQUEST, "Please check all required fields."));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<Map<String, Object>> generic(Exception ex) {
    ResponseStatus annotation = ex.getClass().getAnnotation(ResponseStatus.class);
    HttpStatus status = annotation == null ? HttpStatus.INTERNAL_SERVER_ERROR : annotation.value();
    String message = status.is5xxServerError() ? "Unexpected server error." : ex.getMessage();
    return ResponseEntity.status(status).body(error(status, message));
  }

  private Map<String, Object> error(HttpStatus status, String message) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", status.value());
    body.put("message", message);
    return body;
  }
}
