package com.example.hellospring;

import java.util.Map;
import org.hibernate.LazyInitializationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(LazyInitializationException.class)
    public ResponseEntity<Map<String, String>> lazy(LazyInitializationException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "LazyInitializationException",
                "message", exception.getMessage()));
    }
}
