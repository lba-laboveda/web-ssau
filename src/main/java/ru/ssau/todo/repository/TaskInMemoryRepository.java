package ru.ssau.todo.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import ru.ssau.todo.entity.Task;
import ru.ssau.todo.entity.TaskStatus;

@Repository
public class TaskInMemoryRepository implements TaskRepository {

    private final Map<Long, Task> tasks = new HashMap<>();
    private long count = 1;

    /**
     * Сохраняет новую задачу в хранилище.
     * При сохранении репозиторий обязан присвоить задаче уникальный идентификатор.
     *
     * @param task объект задачи для сохранения (без ID).
     * @return сохраненный экземпляр задачи с назначенным идентификатором.
     * @throws IllegalArgumentException если передана пустая задача (null).
     */
    @Override
    public synchronized Task create(Task task) {
        if (task == null || task.getTitle() == null || task.getTitle().isBlank() || task.getStatus() == null){
            throw new IllegalArgumentException("Task cannot be null");
        }

        System.out.println("Creating task: " + task.getTitle() +
                ", createdBy: " + task.getCreatedBy());

        long newId = count++;
        task.setId(newId);
        task.setCreatedAt(LocalDateTime.now());
        tasks.put(newId, task);
        return task;
    }

    /**
     * Выполняет поиск задачи по её уникальному идентификатору.
     *
     * @param id уникальный идентификатор задачи.
     * @return {@link Optional}, содержащий найденную задачу,
     *         или пустой Optional, если задача с таким ID не найдена.
     */
    @Override
    public Optional<Task> findById(long id) {
        Task task = tasks.get(id);
        return task == null ? Optional.empty() : Optional.of(task);
    }

    /**
     * Возвращает список всех задач конкретного пользователя, созданных в указанном
     * временном диапазоне.
     *
     * @param from   начальная граница даты создания (включительно).
     * @param to     конечная граница даты создания (включительно).
     * @param userId уникальный идентификатор пользователя-владельца.
     * @return список задач, соответствующих критериям поиска. Если ничего не
     *         найдено, возвращается пустой список.
     */
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

    /**
     * Обновляет данные существующей задачи в хранилище.
     * Поиск записи для обновления осуществляется по полю ID, содержащемуся в
     * объекте task.
     *
     * @param task объект задачи с обновленными данными.
     * @throws Exception (специализированное исключение) если задача с таким ID не
     *                   существует.
     */
    @Override
    public void update(Task task) throws Exception {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        long id = task.getId();
        if (tasks.get(id) == null) {
            throw new Exception("Task with id " + id + " not found");
        }

        LocalDateTime existingCreatedAt = task.getCreatedAt();
        task.setCreatedAt(existingCreatedAt);
        tasks.put(id, task);
    }

    /**
     * Удаляет задачу из хранилища по её идентификатору.
     *
     * @param id идентификатор задачи, которую необходимо удалить.
     */
    @Override
    public void deleteById(long id) {
        tasks.remove(id);
    }

    /**
     * Подсчитывает количество "активных" задач для конкретного пользователя.
     * Активной считается задача, находящаяся в статусе OPEN или IN_PROGRESS.
     *
     * @param userId идентификатор пользователя.
     * @return количество активных задач.
     */
    @Override
    public long countActiveTasksByUserId(long userId) {
        long count = 0;

        for (Task task : tasks.values()) {
            if (task.getCreatedBy() == userId) {
                TaskStatus status = task.getStatus();
                if (status == TaskStatus.OPEN || status == TaskStatus.IN_PROGRESS) {
                    count++;
                }
            }
        }

        return count;
    }
}
