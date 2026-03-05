package ru.ssau.todo.exception;

public class TaskBusinessException extends TaskException {
    
    private static final String ERROR_CODE = "BUSINESS_RULE_VIOLATION";

    public TaskBusinessException(String message) {
        super(message, ERROR_CODE, null);
    }

    public TaskBusinessException(String message, Long taskId) {
        super(message, ERROR_CODE, taskId);
    }

    public TaskBusinessException(String message, Throwable cause) {
        super(message, ERROR_CODE, null, cause);
    }
}