package com.example.lab2.task;

import com.example.lab2.repository.CrudRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TaskRepository extends CrudRepository<Task, Long> {

    protected TaskRepository(JdbcTemplate jdbcTemplate) {
        super(Task.class, jdbcTemplate);
    }
}
