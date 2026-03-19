package ru.ssau.todo.exception;

public class TokenExpiredException extends TokenException {
    public TokenExpiredException() {
        super("Token has expired");
    }
}