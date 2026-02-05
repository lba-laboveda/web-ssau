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
import ru.ssau.todo.repository.TaskRepository;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

     /**
     * Создать новую задачу
     * POST /tasks
     * Поля id и createdAt из тела запроса игнорируются
     */
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        if (task == null || task.getTitle() == null || task.getTitle().isBlank() || task.getStatus() == null) {
        return ResponseEntity.badRequest().build();
    }
        Task created = taskRepository.create(task);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Location", "/tasks/" + created.getId())
                .body(created);
    }

     /**
     * Найти задачу по ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable long id) {
        return taskRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Получить все задачи пользователя в диапазоне дат
     * GET /tasks?from={from}&to={to}&userId={userId}
     * from/to - опциональные параметры
     */
    @GetMapping
    public ResponseEntity<List<Task>> getTasks(
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            @RequestParam long userId) {

        List<Task> tasks = taskRepository.findAll(from, to, userId);
        return ResponseEntity.ok(tasks);
    }
   

    /**
     * Обновить задачу
     * PUT /tasks/{id}
     * Поле id из тела запроса игнорируется, используется только из URL
     */

@PutMapping("/{id}")
public ResponseEntity<?> updateTask(
        @PathVariable long id,
        @RequestBody Task task) {

    task.setId(id);

    try {
        taskRepository.update(task);
        return ResponseEntity.ok(task);
    } catch (Exception e) {
        return ResponseEntity.notFound().build();
    }
}

    /**
     * Удалить задачу
     * DELETE /tasks/{id}
     * 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable long id) {
        taskRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Подсчитать активные задачи пользователя
     * Активные: OPEN или IN_PROGRESS
     * GET /tasks/active/count?userId={userId}
     */
    @GetMapping("/active/count")
    public ResponseEntity<Long> countActiveTasks(@RequestParam long userId) {
        long count = taskRepository.countActiveTasksByUserId(userId);
        return ResponseEntity.ok(count);
    }
}