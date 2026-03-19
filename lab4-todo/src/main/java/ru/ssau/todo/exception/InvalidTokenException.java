package ru.ssau.todo.exception;

public class InvalidTokenException extends TokenException {
    public InvalidTokenException(String reason) {
        super("Invalid token: " + reason);
    }
}