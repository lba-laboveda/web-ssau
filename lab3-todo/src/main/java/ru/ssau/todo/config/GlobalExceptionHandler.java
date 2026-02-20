package ru.ssau.todo.config;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import ru.ssau.todo.exception.TaskBusinessException;
import ru.ssau.todo.exception.TaskNotFoundException;
import ru.ssau.todo.exception.TaskValidationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private Map<String, Object> createBaseResponse(HttpStatus status, String error, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", error);
        response.put("message", message);
        return response;
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(TaskNotFoundException e) {
        Map<String, Object> response = createBaseResponse(
            HttpStatus.NOT_FOUND, 
            "Not Found", 
            e.getMessage()
        );
        response.put("errorCode", e.getErrorCode());
        response.put("taskId", e.getTaskId());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(TaskValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(TaskValidationException e) {
        Map<String, Object> response = createBaseResponse(
            HttpStatus.BAD_REQUEST, 
            "Validation Error", 
            e.getMessage()
        );
        response.put("errorCode", e.getErrorCode());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(TaskBusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(TaskBusinessException e) {
        Map<String, Object> response = createBaseResponse(
            HttpStatus.BAD_REQUEST, 
            "Business Rule Violation", 
            e.getMessage()
        );
        response.put("errorCode", e.getErrorCode());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = createBaseResponse(
            HttpStatus.BAD_REQUEST, 
            "Validation Error", 
            "Validation failed"
        );
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        ex.getBindingResult().getGlobalErrors().forEach(error -> 
            errors.put("dateRange", error.getDefaultMessage())
        );
        
        response.put("errors", errors);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception e) {
        Map<String, Object> response = createBaseResponse(
            HttpStatus.INTERNAL_SERVER_ERROR, 
            "Internal Server Error", 
            "An unexpected error occurred"
        );
        log.error("Unexpected error occurred", e);

        return ResponseEntity.internalServerError().body(response);
    }
}