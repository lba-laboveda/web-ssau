package ru.ssau.todo.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.ssau.todo.entity.Task;
import ru.ssau.todo.entity.TaskStatus;

public class TaskDto {
    
    private Long id;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotNull(message = "Status is required")
    private TaskStatus status;
    
    @NotNull(message = "CreatedBy is required")
    private Long createdBy;
    
    private LocalDateTime createdAt;

    public TaskDto() {}

    public static TaskDto fromEntity(Task task) {
        if (task == null) return null;
        
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setStatus(task.getStatus());
        dto.setCreatedBy(task.getCreatedBy() != null ? task.getCreatedBy().getId() : null);
        dto.setCreatedAt(task.getCreatedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}