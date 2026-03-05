package ru.ssau.todo.exception;

public class UserNotFoundException extends TaskException {
    private static final String ERROR_CODE = "USER_NOT_FOUND";

    public UserNotFoundException(Long userId) {
        super(String.format("User with id %d not found", userId), ERROR_CODE, userId);
    }

    public UserNotFoundException(String username) {
        super(String.format("User with username '%s' not found", username), ERROR_CODE, null);
    }
}