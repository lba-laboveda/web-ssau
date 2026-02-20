package ru.ssau.todo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.ssau.todo.entity.Task;
import ru.ssau.todo.entity.TaskStatus;
import ru.ssau.todo.exception.TaskBusinessException;
import ru.ssau.todo.exception.TaskNotFoundException;
import ru.ssau.todo.repository.TaskRepository;

@Service
public class TaskService {

    private static final int MAX_ACTIVE_TASKS = 10;
    private static final long DELETE_RESTRICTION_MINUTES = 5;
    private static final Set<TaskStatus> ACTIVE_STATUSES = Set.of(TaskStatus.OPEN, TaskStatus.IN_PROGRESS);
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    public Task createTask(Task task) {
        validateTaskForCreation(task);
        checkActiveTasksLimit(task.getCreatedBy());
        return taskRepository.create(task);
    }

    @Transactional
    public Task updateTask(Task task) {
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
        Optional<Task> taskOptional = taskRepository.findById(id);
        taskOptional.ifPresent(this::validateTaskForDeletion);
        taskRepository.deleteById(id);
    }

    private void validateTaskForCreation(Task task) {
        if (task == null) {
            throw new TaskBusinessException("Task cannot be null");
        }
        if (task.getCreatedBy() == null) {
            throw new TaskBusinessException("CreatedBy is required");
        }
    }

    private void validateTaskForDeletion(Task task) {
        LocalDateTime createdAt = task.getCreatedAt();
        LocalDateTime earliestAllowedDeletionTime = LocalDateTime.now().minusMinutes(DELETE_RESTRICTION_MINUTES);

        if (createdAt.isAfter(earliestAllowedDeletionTime)) {
            throw new TaskBusinessException(
                    String.format("Cannot delete task %d created less than %d minutes ago",
                            task.getId(), DELETE_RESTRICTION_MINUTES));
        }
    }

    private void checkActiveTasksLimit(Long userId) {
        long activeCount = taskRepository.countActiveTasksByUserId(userId);
        if (activeCount >= MAX_ACTIVE_TASKS) {
            throw new TaskBusinessException(
                    String.format("User %d already has %d active tasks (maximum %d)",
                            userId, activeCount, MAX_ACTIVE_TASKS));
        }
    }

    private boolean isBecomingActive(TaskStatus newStatus, TaskStatus oldStatus) {
        return isActiveStatus(newStatus) && !isActiveStatus(oldStatus);
    }

    private boolean isActiveStatus(TaskStatus status) {
        return ACTIVE_STATUSES.contains(status);
    }

    private Task findExistingTask(long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    private void protectImmutableFields(Task target, Task source) {
        target.setCreatedBy(source.getCreatedBy());
        target.setCreatedAt(source.getCreatedAt());
    }

    public long countActiveTasksByUserId(long userId) {
        return taskRepository.countActiveTasksByUserId(userId);
    }

    public Task findById(long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    public List<Task> findAll(LocalDateTime from, LocalDateTime to, long userId) {
        return taskRepository.findAll(from, to, userId);
    }
}