package ru.ssau.todo.repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import ru.ssau.todo.entity.Task;
import ru.ssau.todo.entity.TaskStatus;

@Repository
@Profile("jdbc")
public class TaskJdbcRepository implements TaskRepository {

    private final JdbcTemplate jdbcTemplate;

    public TaskJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Task> taskRowMapper = (rs, rowNum) -> {
        Task task = new Task();
        task.setId(rs.getLong("id"));
        task.setTitle(rs.getString("title"));
        task.setStatus(TaskStatus.valueOf(rs.getString("status")));
        task.setCreatedBy(rs.getLong("created_by"));
        task.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return task;
    };

    @Override
    public Task create(Task task) {
        String sql = "INSERT INTO task (title, status, created_by, created_at) VALUES (?, ?, ?, ?)";

        LocalDateTime now = LocalDateTime.now();

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getStatus().name());
            ps.setLong(3, task.getCreatedBy());
            ps.setTimestamp(4, Timestamp.valueOf(now));
            return ps;
        }, keyHolder);

        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null && keys.containsKey("id")) {
            task.setId(((Number) keys.get("id")).longValue());
        } else {
            throw new RuntimeException("Could not obtain generated ID");
        }

        task.setCreatedAt(now);
        return task;
    }

    @Override
    public Optional<Task> findById(long id) {
        String sql = "SELECT * FROM task WHERE id = ?";
        try {
            Task task = jdbcTemplate.queryForObject(sql, taskRowMapper, id);
            return Optional.ofNullable(task);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Task> findAll(LocalDateTime from, LocalDateTime to, long userId) {
        String sql = "SELECT * FROM task WHERE created_by = ? " +
                "AND created_at BETWEEN COALESCE(?, '1970-01-01') AND COALESCE(?, '9999-12-31') " +
                "ORDER BY created_at DESC";

        return jdbcTemplate.query(sql, taskRowMapper, userId, from, to);
    }

    @Override
    public void update(Task task) throws Exception {
        String sql = "UPDATE task SET title = ?, status = ? WHERE id = ?";
        int updated = jdbcTemplate.update(sql, task.getTitle(), task.getStatus().name(), task.getId());

        if (updated == 0) {
            throw new Exception("Task with id " + task.getId() + " not found");
        }
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM task WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public long countActiveTasksByUserId(long userId) {
        String sql = "SELECT COUNT(*) FROM task WHERE created_by = ? AND status IN (?, ?)";
        return jdbcTemplate.queryForObject(
                sql,
                Long.class,
                userId,
                TaskStatus.OPEN.name(),
                TaskStatus.IN_PROGRESS.name());
    }
}