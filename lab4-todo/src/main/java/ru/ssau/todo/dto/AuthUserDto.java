package ru.ssau.todo.dto;

import java.util.List;

public class AuthUserDto {
    private String username;
    private List<String> roles;

    public AuthUserDto() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}