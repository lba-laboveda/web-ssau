
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
import ru.ssau.todo.service.TaskService;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody Task task) {

        validateTask(task);
        Task created = taskService.createTask(task);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Location", "/tasks/" + created.getId())
                .body(created);

    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable long id) {
        return taskService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Task>> getTasks(
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            @RequestParam long userId) {

        List<Task> tasks = taskService.findAll(from, to, userId);
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable long id, @RequestBody Task task) {
            task.setId(id);
            validateTask(task);
        try {
            Task updated = taskService.updateTask(task);
            return ResponseEntity.ok(updated);

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable long id) {

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
            throw new TaskException("Task cannot be null");
        }
        if (task.getTitle() == null || task.getTitle().isBlank()) {
            throw new TaskException("Title is required");
        }
        if (task.getStatus() == null) {
            throw new TaskException("Status is required");
        }
        if (task.getCreatedBy() == null) {
            throw new TaskException("CreatedBy is required");
        }
    }
}