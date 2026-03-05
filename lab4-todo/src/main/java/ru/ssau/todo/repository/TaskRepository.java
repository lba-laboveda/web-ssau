package ru.ssau.todo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.ssau.todo.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {
    
    @Query(value = "SELECT * FROM task WHERE created_by = :userId " +
       "AND (cast(:from as timestamp) IS NULL OR created_at >= cast(:from as timestamp)) " +
       "AND (cast(:to as timestamp) IS NULL OR created_at <= cast(:to as timestamp)) " +
       "ORDER BY created_at DESC", 
       nativeQuery = true)
List<Task> findTasksByDateRangeAndUser(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to,
        @Param("userId") Long userId);
    @Query("SELECT COUNT(t) FROM Task t WHERE t.createdBy.id = :userId " +
           "AND (t.status = 'OPEN' OR t.status = 'IN_PROGRESS')")
    long countActiveTasksByUserId(@Param("userId") Long userId);
}