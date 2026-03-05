// ru.ssau.todo.dto.UserDto.java
package ru.ssau.todo.dto;

import java.util.List;
import jakarta.validation.constraints.NotBlank;

public class UserDto {

    private Long id;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")  // ✅ Валидация пароля
    private String password;  // ✅ Новое поле

    private List<String> roles;

    public UserDto() {}

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    // ✅ ДОБАВЬТЕ ЭТИ МЕТОДЫ:
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}