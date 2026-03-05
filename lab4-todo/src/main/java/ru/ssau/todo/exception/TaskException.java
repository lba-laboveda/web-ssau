package ru.ssau.todo.exception;
public abstract class TaskException extends RuntimeException {
    
    private final String errorCode;
    private final Long taskId;
    private final String detailedMessage;

    protected TaskException(String message, String errorCode, Long taskId) {
        super(message);
        this.errorCode = errorCode;
        this.taskId = taskId;
        this.detailedMessage = buildDetailedMessage(message, errorCode, taskId);
    }

    protected TaskException(String message, String errorCode, Long taskId, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.taskId = taskId;
        this.detailedMessage = buildDetailedMessage(message, errorCode, taskId);
    }

    private String buildDetailedMessage(String message, String errorCode, Long taskId) {
        StringBuilder sb = new StringBuilder(message);
        sb.append(" [Code: ").append(errorCode).append("]");
        if (taskId != null) {
            sb.append(" [Task ID: ").append(taskId).append("]");
        }
        return sb.toString();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Long getTaskId() {
        return taskId;
    }

    @Override
    public String getMessage() {
        return detailedMessage;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + detailedMessage;
    }
}