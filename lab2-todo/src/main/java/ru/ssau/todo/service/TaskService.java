package ru.ssau.todo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.ssau.todo.entity.Task;
import ru.ssau.todo.entity.TaskStatus;
import ru.ssau.todo.repository.TaskRepository;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    public Task createTask(Task task) {
        if (task.getId() != 0) {
            task.setId(0);
        }
        checkActiveTasksLimit(task.getCreatedBy());
        
        return taskRepository.create(task);
    }

    @Transactional
    public Task updateTask(Task task) throws Exception {
        Task existing = findExistingTask(task.getId());
        protectImmutableFields(task, existing);
        if (isBecomingActive(task.getStatus(), existing.getStatus())) {
            checkActiveTasksLimit(existing.getCreatedBy());
        }
        
        taskRepository.update(task);
        return task;
    }

     @Transactional
    public void deleteTask(long id) {
        Optional<Task> task = taskRepository.findById(id);
        if (task.isPresent()) {
            LocalDateTime createdAt = task.get().getCreatedAt();
            if (createdAt.isAfter(LocalDateTime.now().minusMinutes(5))) {
                throw new IllegalStateException("Cannot delete task created less than 5 minutes ago");
            }
        }
        
        taskRepository.deleteById(id);
    }

    public long countActiveTasksByUserId(long userId) {
        return taskRepository.countActiveTasksByUserId(userId);
    }

    public Optional<Task> findById(long id) {
        return taskRepository.findById(id);
    }

    public List<Task> findAll(LocalDateTime from, LocalDateTime to, long userId) {
        return taskRepository.findAll(from, to, userId);
    }

    private Task findExistingTask(long id) throws Exception {
        return taskRepository.findById(id)
            .orElseThrow(() -> new Exception("Task not found"));
    }

    private void checkActiveTasksLimit(Long userId) {
        long activeCount = taskRepository.countActiveTasksByUserId(userId);
        if (activeCount >= 10) {
            throw new IllegalStateException(
                String.format("User %d already has %d active tasks (maximum 10)", 
                userId, activeCount)
            );
        }
    }

    private void protectImmutableFields(Task target, Task source) {
        target.setCreatedBy(source.getCreatedBy());
        target.setCreatedAt(source.getCreatedAt());
    }

    private boolean isBecomingActive(TaskStatus newStatus, TaskStatus oldStatus) {
        boolean isNewActive = newStatus == TaskStatus.OPEN || newStatus == TaskStatus.IN_PROGRESS;
        boolean wasActive = oldStatus == TaskStatus.OPEN || oldStatus == TaskStatus.IN_PROGRESS;
        return isNewActive && !wasActive;
    }

    
}