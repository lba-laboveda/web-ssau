package ru.ssau.todo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.ssau.todo.dto.TaskDto;
import ru.ssau.todo.entity.Task;
import ru.ssau.todo.entity.TaskStatus;
import ru.ssau.todo.entity.User;
import ru.ssau.todo.exception.TaskBusinessException;
import ru.ssau.todo.exception.TaskNotFoundException;
import ru.ssau.todo.exception.UserNotFoundException;
import ru.ssau.todo.repository.TaskRepository;
import ru.ssau.todo.repository.UserRepository;

@Service
public class TaskService {

    private static final int MAX_ACTIVE_TASKS = 10;
    private static final long DELETE_RESTRICTION_MINUTES = 5;
    private static final Set<TaskStatus> ACTIVE_STATUSES = Set.of(TaskStatus.OPEN, TaskStatus.IN_PROGRESS);

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }
    @Transactional
    public TaskDto createTask(TaskDto taskDto) {
        User user = userRepository.findById(taskDto.getCreatedBy())
                .orElseThrow(() -> new UserNotFoundException(taskDto.getCreatedBy()));

        checkActiveTasksLimit(user.getId());
        Task task = new Task();
        task.setTitle(taskDto.getTitle());
        task.setStatus(taskDto.getStatus());
        task.setCreatedBy(user);
        task.setCreatedAt(LocalDateTime.now());

        Task savedTask = taskRepository.save(task);
        return TaskDto.fromEntity(savedTask);
    }

    @Transactional
    public TaskDto updateTask(TaskDto taskDto) {
        Task existing = taskRepository.findById(taskDto.getId())
                .orElseThrow(() -> new TaskNotFoundException(taskDto.getId()));

        if (!existing.getCreatedBy().getId().equals(taskDto.getCreatedBy())) {
            throw new TaskBusinessException("Cannot change task owner");
        }

        if (isBecomingActive(taskDto.getStatus(), existing.getStatus())) {
            checkActiveTasksLimit(existing.getCreatedBy().getId());
        }

        existing.setTitle(taskDto.getTitle());
        existing.setStatus(taskDto.getStatus());

        Task updatedTask = taskRepository.save(existing);
        return TaskDto.fromEntity(updatedTask);
    }

    @Transactional
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        if (task.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(DELETE_RESTRICTION_MINUTES))) {
            throw new TaskBusinessException(
                    String.format("Cannot delete task %d created less than %d minutes ago",
                            id, DELETE_RESTRICTION_MINUTES)
            );
        }

        taskRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public TaskDto findById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        return TaskDto.fromEntity(task);
    }

    @Transactional(readOnly = true)
    public List<TaskDto> findAll(LocalDateTime from, LocalDateTime to, Long userId) {
        if (userId != null) {
            userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(userId));
        }

        List<Task> tasks = taskRepository.findTasksByDateRangeAndUser(from, to, userId);
        return tasks.stream()
                .map(TaskDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long countActiveTasksByUserId(Long userId) {
        return taskRepository.countActiveTasksByUserId(userId);
    }

    private void checkActiveTasksLimit(Long userId) {
        long activeCount = taskRepository.countActiveTasksByUserId(userId);
        if (activeCount >= MAX_ACTIVE_TASKS) {
            throw new TaskBusinessException(
                    String.format("User %d already has %d active tasks (maximum %d)",
                            userId, activeCount, MAX_ACTIVE_TASKS)
            );
        }
    }

    private boolean isBecomingActive(TaskStatus newStatus, TaskStatus oldStatus) {
        return isActiveStatus(newStatus) && !isActiveStatus(oldStatus);
    }

    private boolean isActiveStatus(TaskStatus status) {
        return ACTIVE_STATUSES.contains(status);
    }
}