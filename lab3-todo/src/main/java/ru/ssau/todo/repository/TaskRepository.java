package ru.ssau.todo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.ssau.todo.entity.Task;
import ru.ssau.todo.entity.TaskStatus;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query(value = "SELECT * FROM task WHERE created_by = :userId " +
    "AND created_at >= COALESCE(CAST(:from AS TIMESTAMP), CAST('1900-01-01' AS TIMESTAMP)) " +
    "AND created_at <= COALESCE(CAST(:to AS TIMESTAMP), CAST('2100-12-31' AS TIMESTAMP)) " +
    "ORDER BY created_at DESC",
    nativeQuery = true)
    List<Task> findTasksByDateRangeAndUser(
            @Param("userId") Long userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
            

    @Query("SELECT COUNT(t) FROM Task t WHERE t.createdBy.id = :userId " +
           "AND t.status IN :statuses")
    long countActiveTasksByUserId(
            @Param("userId") Long userId,
            @Param("statuses") List<TaskStatus> statuses);

    default long countActiveTasksByUserId(Long userId) {
        return countActiveTasksByUserId(userId, List.of(TaskStatus.OPEN, TaskStatus.IN_PROGRESS));
    }
}