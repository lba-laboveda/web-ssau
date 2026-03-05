package ru.ssau.todo.exception;

public class TaskValidationException extends TaskException {
    
    public TaskValidationException(String message) {
        super(message, "VALIDATION_ERROR", null);
    }
    
    public TaskValidationException(String message, String field) {
        super("Field '" + field + "': " + message, "VALIDATION_ERROR", null);
    }
}