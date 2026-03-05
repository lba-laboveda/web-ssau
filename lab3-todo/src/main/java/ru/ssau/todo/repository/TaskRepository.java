package ru.ssau.todo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.ssau.todo.entity.Task;
import ru.ssau.todo.entity.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // 1. Native Query = true (по требованию)
    // 2. Фильтрация по дате и userId (по требованию)
    // 3. Уникальное имя метода (не конфликтует с JpaRepository)
    // 4. Есть сортировка (хороший тон)

    @Query(value = "SELECT * FROM task WHERE created_by = :userId " +
            "AND (:from IS NULL OR created_at >= :from) " +
            "AND (:to IS NULL OR created_at <= :to) " +
            "ORDER BY created_at DESC",
            nativeQuery = true)
    List<Task> findTasksByDateRangeAndUser(
            @Param("from") LocalDateTime from,   // 1-й параметр
            @Param("to") LocalDateTime to,       // 2-й параметр
            @Param("userId") Long userId);       // 3-й параметр

    // 1. JPQL (nativeQuery не указан, значит false по умолчанию)
    // 2. Подсчет активных задач (логика внутри)
    // 3. Использование Enum вместо строк (исправление弱点 Варианта 1)
    @Query("SELECT COUNT(t) FROM Task t WHERE t.createdBy.id = :userId " +
            "AND t.status IN :statuses")
    long countActiveTasksByUserId(
            @Param("userId") Long userId,
            @Param("statuses") List<TaskStatus> statuses);

    // Удобный метод для вызова из сервиса, чтобы не передавать статусы каждый раз
    default long countActiveTasksByUserId(Long userId) {
        return countActiveTasksByUserId(userId, List.of(TaskStatus.OPEN, TaskStatus.IN_PROGRESS));
    }
}