package com.example.lab2.task;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TaskRepository {

    private final JdbcTemplate jdbcTemplate;

    public TaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long create(Task task) {
        String sql = """
                INSERT INTO tasks (title, description, priority, completed, date, recursive_type)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setInt(3, task.getPriority());
            ps.setBoolean(4, task.isCompleted());
            ps.setObject(5, task.getDate());
            ps.setInt(6, task.getRecursiveType());
            return ps;
        }, keyHolder);

        Long id = extractId(keyHolder);
        task.setId(id);
        return id;
    }

    private Long extractId(KeyHolder keyHolder) {
        Number key = keyHolder.getKey();
        if (key != null) {
            return key.longValue();
        }

        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null && keys.containsKey("id")) {
            Object idValue = keys.get("id");
            if (idValue instanceof Number number) {
                return number.longValue();
            }
        }

        throw new IllegalStateException("Failed to retrieve generated id for task");
    }

    public Optional<Task> findById(Long id) {
        String sql = "SELECT * FROM tasks WHERE id = ?";
        List<Task> result = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Task.class), id);
        return result.stream().findFirst();
    }

    public List<Task> findAll() {
        return jdbcTemplate.query("SELECT * FROM tasks ORDER BY id", new BeanPropertyRowMapper<>(Task.class));
    }

    public int update(Task task) {
        String sql = """
                UPDATE tasks
                SET title = ?, description = ?, priority = ?, completed = ?, date = ?, recursive_type = ?
                WHERE id = ?
                """;
        return jdbcTemplate.update(sql,
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.isCompleted(),
                task.getDate(),
                task.getRecursiveType(),
                task.getId());
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM tasks WHERE id = ?", id);
    }

    public List<Task> findByPriority(int priority) {
        return jdbcTemplate.query(
                "SELECT * FROM tasks WHERE priority = ? ORDER BY date, id",
                new BeanPropertyRowMapper<>(Task.class),
                priority);
    }

    public List<Task> findByCompleted(boolean completed) {
        return jdbcTemplate.query(
                "SELECT * FROM tasks WHERE completed = ? ORDER BY date, id",
                new BeanPropertyRowMapper<>(Task.class),
                completed);
    }

    public List<Task> findByDateBetween(LocalDate from, LocalDate to) {
        return jdbcTemplate.query(
                "SELECT * FROM tasks WHERE date BETWEEN ? AND ? ORDER BY date, priority DESC",
                new BeanPropertyRowMapper<>(Task.class),
                from, to);
    }

    public List<Task> findByTitleContaining(String titlePart) {
        return jdbcTemplate.query(
                "SELECT * FROM tasks WHERE LOWER(title) LIKE LOWER(?) ORDER BY id",
                new BeanPropertyRowMapper<>(Task.class),
                "%" + titlePart + "%");
    }

    public List<Task> findByPriorityAndCompleted(int priority, boolean completed) {
        return jdbcTemplate.query(
                "SELECT * FROM tasks WHERE priority = ? AND completed = ? ORDER BY date",
                new BeanPropertyRowMapper<>(Task.class),
                priority, completed);
    }
}
