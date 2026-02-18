package ru.ssau.todo.controller;

public class TaskException extends RuntimeException {
    private final String errorCode;

    public TaskException(String message) {
        super(message);
        this.errorCode = "TASK_ERROR";
    }

    public TaskException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() { return errorCode; }
}