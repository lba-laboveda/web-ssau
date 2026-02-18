package ru.ssau.todo.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import ru.ssau.todo.entity.Task;
import ru.ssau.todo.entity.TaskStatus;

@Repository
@Profile("in-memory")
public class TaskInMemoryRepository implements TaskRepository {

    private final Map<Long, Task> tasks = new HashMap<>();
    private long count = 1;

    @Override
    public synchronized Task create(Task task) {
        if (task == null || task.getTitle() == null || task.getTitle().isBlank() || task.getStatus() == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }

        long newId = count++;
        task.setId(newId);
        task.setCreatedAt(LocalDateTime.now());
        tasks.put(newId, task);
        return task;
    }

    @Override
    public Optional<Task> findById(long id) {
        Task task = tasks.get(id);
        return task == null ? Optional.empty() : Optional.of(task);
    }

    @Override
    public List<Task> findAll(LocalDateTime from, LocalDateTime to, long userId) {
        LocalDateTime startDate = (from != null) ? from : LocalDateTime.MIN;
        LocalDateTime endDate = (to != null) ? to : LocalDateTime.MAX;
        List<Task> result = new ArrayList<>();

        for (Task task : tasks.values()) {
            if (task.getCreatedBy() == null || task.getCreatedBy() != userId) {
                continue;
            }
            LocalDateTime createdAt = task.getCreatedAt();
            if (createdAt != null && !createdAt.isBefore(startDate) && !createdAt.isAfter(endDate)) {
                result.add(task);
            }
        }

        return result;
    }

    @Override
    public void update(Task task) throws Exception {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        long id = task.getId();
        Task existingTask = tasks.get(id);
        if (existingTask == null) {
            throw new Exception("Task with id " + id + " not found");
        }

        LocalDateTime newCreatedAt = existingTask.getCreatedAt();
        task.setCreatedAt(newCreatedAt);
        tasks.put(id, task);
    }

    @Override
    public void deleteById(long id) {
        tasks.remove(id);
    }

    @Override
    public long countActiveTasksByUserId(long userId) {
        long countActive = 0;
        for (Task task : tasks.values()) {
            if (task.getCreatedBy() == userId) {
                TaskStatus status = task.getStatus();
                if (status == TaskStatus.OPEN || status == TaskStatus.IN_PROGRESS) {
                    countActive++;
                }
            }
        }

        return countActive;
    }
}
