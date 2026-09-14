package com.example.lab2.task;

import com.example.lab2.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class TaskRepository extends CrudRepository<Task, Long> {

    protected TaskRepository(DataSource dataSource) {
        super(Task.class, dataSource);
    }
}
