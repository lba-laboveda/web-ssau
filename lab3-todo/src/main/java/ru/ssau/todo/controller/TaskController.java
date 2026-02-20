package ru.ssau.todo.controller;

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

import jakarta.validation.Valid;
import ru.ssau.todo.dto.TaskDto;
import ru.ssau.todo.dto.TaskFilterDto;
import ru.ssau.todo.service.TaskService;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody TaskDto taskDto) {
        TaskDto created = taskService.createTask(taskDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Location", "/tasks/" + created.getId())
                .body(created);
    }

    @GetMapping("/{id}")
    public TaskDto getTaskById(@PathVariable long id) {
        return taskService.findById(id);
    }

    @GetMapping
    public ResponseEntity<List<TaskDto>> getTasks(@Valid TaskFilterDto filter) {
        List<TaskDto> tasks = taskService.findAll(
            filter.getFrom(), 
            filter.getTo(), 
            filter.getUserId()
        );
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> updateTask(
            @PathVariable long id, 
            @Valid @RequestBody TaskDto taskDto) {
        taskDto.setId(id);
        TaskDto updated = taskService.updateTask(taskDto);
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
}