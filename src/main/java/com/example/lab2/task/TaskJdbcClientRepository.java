package com.example.lab2.task;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TaskJdbcClientRepository {

    private final JdbcClient jdbcClient;

    public TaskJdbcClientRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Long create(Task task) {
        String sql = """
                INSERT INTO tasks (title, description, priority, completed, date, recursive_type)
                VALUES (:title, :description, :priority, :completed, :date, :recursiveType)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcClient.sql(sql)
                .param("title", task.getTitle())
                .param("description", task.getDescription())
                .param("priority", task.getPriority())
                .param("completed", task.isCompleted())
                .param("date", task.getDate())
                .param("recursiveType", task.getRecursiveType())
                .update(keyHolder, "id");

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

        throw new IllegalStateException("Failed to retrieve generated id for task (JdbcClient)");
    }

    public Optional<Task> findById(Long id) {
        return jdbcClient.sql("SELECT * FROM tasks WHERE id = :id")
                .param("id", id)
                .query(Task.class)
                .optional();
    }

    public List<Task> findAll() {
        return jdbcClient.sql("SELECT * FROM tasks ORDER BY id")
                .query(Task.class)
                .list();
    }

    public int update(Task task) {
        return jdbcClient.sql("""
                        UPDATE tasks
                        SET title = :title,
                            description = :description,
                            priority = :priority,
                            completed = :completed,
                            date = :date,
                            recursive_type = :recursiveType
                        WHERE id = :id
                        """)
                .param("title", task.getTitle())
                .param("description", task.getDescription())
                .param("priority", task.getPriority())
                .param("completed", task.isCompleted())
                .param("date", task.getDate())
                .param("recursiveType", task.getRecursiveType())
                .param("id", task.getId())
                .update();
    }

    public int deleteById(Long id) {
        return jdbcClient.sql("DELETE FROM tasks WHERE id = :id")
                .param("id", id)
                .update();
    }

    public List<Task> findByPriority(int priority) {
        return jdbcClient.sql("SELECT * FROM tasks WHERE priority = :priority ORDER BY date, id")
                .param("priority", priority)
                .query(Task.class)
                .list();
    }

    public List<Task> findByCompleted(boolean completed) {
        return jdbcClient.sql("SELECT * FROM tasks WHERE completed = :completed ORDER BY date, id")
                .param("completed", completed)
                .query(Task.class)
                .list();
    }

    public List<Task> findByDateBetween(LocalDate from, LocalDate to) {
        return jdbcClient.sql("""
                        SELECT * FROM tasks
                        WHERE date BETWEEN :from AND :to
                        ORDER BY date, priority DESC
                        """)
                .param("from", from)
                .param("to", to)
                .query(Task.class)
                .list();
    }

    public List<Task> findByTitleContaining(String titlePart) {
        return jdbcClient.sql("""
                        SELECT * FROM tasks
                        WHERE LOWER(title) LIKE LOWER(:pattern)
                        ORDER BY id
                        """)
                .param("pattern", "%" + titlePart + "%")
                .query(Task.class)
                .list();
    }

    public List<Task> findByPriorityAndCompleted(int priority, boolean completed) {
        return jdbcClient.sql("""
                        SELECT * FROM tasks
                        WHERE priority = :priority AND completed = :completed
                        ORDER BY date
                        """)
                .param("priority", priority)
                .param("completed", completed)
                .query(Task.class)
                .list();
    }
}
