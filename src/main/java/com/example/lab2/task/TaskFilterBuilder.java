package com.example.lab2.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Component
@Scope("prototype")
public class TaskFilterBuilder {
    private TaskRepository repository;
    private final List<Predicate<Task>> conditions = new ArrayList<>();

    @Autowired
    public void setRepository(TaskRepository taskRepository) {
        this.repository = taskRepository;
    }

    public TaskFilterBuilder byPriority(int priority) {
        conditions.add(task -> task.getPriority() == priority);
        return this;
    }

    public TaskFilterBuilder notCompleted() {
        conditions.add(task -> !task.isCompleted());
        return this;
    }

    public TaskFilterBuilder completed() {
        conditions.add(task -> task.isCompleted());
        return this;
    }

    public TaskFilterBuilder byRecursiveType(int recursiveType) {
        conditions.add(task -> task.getRecursiveType() == recursiveType);
        return this;
    }

    public TaskFilterBuilder byDateBetween(LocalDate from, LocalDate to) {
        conditions.add(task -> !task.getDate().isBefore(from) && !task.getDate().isAfter(to));
        return this;
    }

    public List<Task> apply() {
        return repository.findAll().stream()
                .filter(task -> conditions.stream().allMatch(c -> c.test(task)))
                .toList();
    }
}