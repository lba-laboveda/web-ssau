package ru.ssau.todo.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ru.ssau.todo.entity.Task;
import ru.ssau.todo.exception.TaskValidationException;
import ru.ssau.todo.service.TaskService;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        validateTask(task);
        Task created = taskService.createTask(task);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Location", "/tasks/" + created.getId())
                .body(created);
    }

    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable long id) {
        return taskService.findById(id);
    }

    @GetMapping
    public ResponseEntity<List<Task>> getTasks(
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            @RequestParam long userId) {

        validateTime(from, to);
        List<Task> tasks = taskService.findAll(from, to, userId);
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable long id, @RequestBody Task task) {
        task.setId(id);
        validateTask(task);
        Task updated = taskService.updateTask(task);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/active/count")
    public ResponseEntity<Long> countActiveTasks(@RequestParam long userId) {
        long count = taskService.countActiveTasksByUserId(userId);
        return ResponseEntity.ok(count);
    }

    private void validateTask(Task task) {
        if (task == null) {
            throw new TaskValidationException("Task cannot be null");
        }
        if (task.getTitle() == null || task.getTitle().isBlank()) {
            throw new TaskValidationException("Title is required", "title");
        }
        if (task.getStatus() == null) {
            throw new TaskValidationException("Status is required", "status");
        }
        if (task.getCreatedBy() == null) {
            throw new TaskValidationException("CreatedBy is required", "createdBy");
        }
    }

    private void validateTime(LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null) {
            if (from.isAfter(to)) {
                throw new TaskValidationException(
                        String.format("Start date [%s] cannot be after end date [%s]", from, to),
                        "dateRange");
            }
        }
    }
}