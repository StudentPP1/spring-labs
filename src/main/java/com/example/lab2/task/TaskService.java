package com.example.lab2.task;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class TaskService {
    private final TaskRepository repository;
    private final ObjectFactory<TaskFilterBuilder> filterBuilderFactory;

    public TaskService(TaskRepository repository,
                       ObjectFactory<TaskFilterBuilder> filterBuilderFactory) {
        this.repository = repository;
        this.filterBuilderFactory = filterBuilderFactory;
    }

    public void createTask(Task task) {
        Objects.requireNonNull(task, "Task must not be null");
        repository.create(task);
    }

    public void updateTask(Task task) {
        Objects.requireNonNull(task, "Task must not be null");
        repository.update(task);
    }

    public void deleteTask(Long id) {
        Objects.requireNonNull(id, "Id must not be null");
        repository.delete(id);
    }

    public Task getTaskById(Long id) {
        Objects.requireNonNull(id, "Id must not be null");
        try {
            return repository.find(id);
        } catch (EmptyResultDataAccessException e) {
            throw new TaskNotFoundException("Task with id " + id + " not found");
        }
    }

    public void markAsCompleted(Long id) {
        Task task = getTaskById(id);
        task.setCompleted(true);
        repository.update(task);

        RecursiveType type = RecursiveType.fromValue(task.getRecursiveType());
        if (type != RecursiveType.NONE) {
            Task nextTask = new Task();
            nextTask.setTitle(task.getTitle());
            nextTask.setDescription(task.getDescription());
            nextTask.setPriority(task.getPriority());
            nextTask.setRecursiveType(task.getRecursiveType());
            nextTask.setDate(calculateNextDate(task.getDate(), type));
            nextTask.setCompleted(false);
            repository.create(nextTask);
        }
    }

    private LocalDate calculateNextDate(LocalDate currentDate, RecursiveType type) {
        return switch (type) {
            case DAY -> currentDate.plusDays(1);
            case WEEK -> currentDate.plusWeeks(1);
            case NONE -> currentDate;
        };
    }

    public List<Task> filterTasks(Integer priority, Boolean completed, String sort) {
        TaskFilterBuilder builder = filterBuilderFactory.getObject();
        if (priority != null) {
            builder.byPriority(priority);
        }
        if (completed != null) {
            if (completed) {
                builder.completed();
            } else {
                builder.notCompleted();
            }
        }
        List<Task> tasks = builder.apply();
        return switch (sort == null ? "" : sort) {
            case "priority" -> tasks.stream().sorted(Comparator.comparingInt(Task::getPriority)).toList();
            case "date" -> tasks.stream().sorted(Comparator.comparing(Task::getDate)).toList();
            default -> tasks;
        };
    }

    public List<Task> getTasksBetween(LocalDate from, LocalDate to) {
        return filterBuilderFactory.getObject()
                .byDateBetween(from, to)
                .apply()
                .stream()
                .sorted(Comparator.comparingInt(Task::getPriority).reversed())
                .toList();
    }
}