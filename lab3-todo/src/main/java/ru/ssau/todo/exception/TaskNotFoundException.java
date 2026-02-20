package ru.ssau.todo.exception;
public class TaskNotFoundException extends TaskException {
    
    private static final String ERROR_CODE = "TASK_NOT_FOUND";

    public TaskNotFoundException(Long taskId) {
        super(
            String.format("Task with id %d not found", taskId),
            ERROR_CODE,
            taskId
        );
    }

    public TaskNotFoundException(String message, Long taskId) {
        super(message, ERROR_CODE, taskId);
    }
}