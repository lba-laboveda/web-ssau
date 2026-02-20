package ru.ssau.todo.dto;

import java.util.List;
import java.util.stream.Collectors;

import ru.ssau.todo.entity.User;

public class UserDto {
    private Long id;
    private String username;
    private List<String> roles;

    public UserDto() {}

    public static UserDto fromEntity(User user) {
        if (user == null) return null;
        
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRoles(user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList()));
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}