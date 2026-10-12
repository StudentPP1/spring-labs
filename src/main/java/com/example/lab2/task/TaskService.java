package com.example.lab2.task;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class TaskService {

    private final TaskRepository jdbcTemplateRepo;
    private final TaskJdbcClientRepository jdbcClientRepo;

    public TaskService(TaskRepository jdbcTemplateRepo,
                       TaskJdbcClientRepository jdbcClientRepo) {
        this.jdbcTemplateRepo = jdbcTemplateRepo;
        this.jdbcClientRepo = jdbcClientRepo;
    }

    public Long createTask(Task task) {
        Objects.requireNonNull(task, "Task must not be null");
        return jdbcTemplateRepo.create(task);
    }

    public Long createTaskViaJdbcClient(Task task) {
        Objects.requireNonNull(task, "Task must not be null");
        return jdbcClientRepo.create(task);
    }

    public Task getTaskById(Long id) {
        Objects.requireNonNull(id, "Id must not be null");
        return jdbcTemplateRepo.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task with id " + id + " not found"));
    }

    public List<Task> findAll() {
        return jdbcTemplateRepo.findAll();
    }

    public void updateTask(Task task) {
        Objects.requireNonNull(task, "Task must not be null");
        int updated = jdbcTemplateRepo.update(task);
        if (updated == 0) {
            throw new TaskNotFoundException("Task with id " + task.getId() + " not found");
        }
    }

    public void deleteTask(Long id) {
        Objects.requireNonNull(id, "Id must not be null");
        int deleted = jdbcTemplateRepo.deleteById(id);
        if (deleted == 0) {
            throw new TaskNotFoundException("Task with id " + id + " not found");
        }
    }

    @Transactional
    public void markAsCompleted(Long id) {
        Task task = getTaskById(id);

        if (task.isCompleted()) {
            return;
        }

        task.setCompleted(true);
        jdbcTemplateRepo.update(task);

        RecursiveType type = RecursiveType.fromValue(task.getRecursiveType());
        if (type != RecursiveType.NONE) {
            Task nextTask = new Task();
            nextTask.setTitle(task.getTitle());
            nextTask.setDescription(task.getDescription());
            nextTask.setPriority(task.getPriority());
            nextTask.setRecursiveType(task.getRecursiveType());
            nextTask.setDate(calculateNextDate(task.getDate(), type));
            nextTask.setCompleted(false);

            jdbcClientRepo.create(nextTask);
        }
    }

    @Transactional
    public void markAsCompletedWithPossibleError(Long id, boolean forceError) {
        Task task = getTaskById(id);
        task.setCompleted(true);
        jdbcTemplateRepo.update(task);

        if (forceError) {
            throw new RuntimeException("Simulated error to demonstrate transaction rollback");
        }

        RecursiveType type = RecursiveType.fromValue(task.getRecursiveType());
        if (type != RecursiveType.NONE) {
            Task nextTask = new Task();
            nextTask.setTitle(task.getTitle());
            nextTask.setDescription(task.getDescription());
            nextTask.setPriority(task.getPriority());
            nextTask.setRecursiveType(task.getRecursiveType());
            nextTask.setDate(calculateNextDate(task.getDate(), type));
            nextTask.setCompleted(false);
            jdbcClientRepo.create(nextTask);
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
        List<Task> tasks;

        if (priority != null && completed != null) {
            tasks = jdbcTemplateRepo.findByPriorityAndCompleted(priority, completed);
        } else if (priority != null) {
            tasks = jdbcTemplateRepo.findByPriority(priority);
        } else if (completed != null) {
            tasks = jdbcTemplateRepo.findByCompleted(completed);
        } else {
            tasks = jdbcTemplateRepo.findAll();
        }

        return switch (sort == null ? "" : sort) {
            case "priority" -> tasks.stream()
                    .sorted(Comparator.comparingInt(Task::getPriority))
                    .toList();
            case "date" -> tasks.stream()
                    .sorted(Comparator.comparing(Task::getDate))
                    .toList();
            default -> tasks;
        };
    }

    public List<Task> getTasksBetween(LocalDate from, LocalDate to) {
        return jdbcClientRepo.findByDateBetween(from, to).stream()
                .sorted(Comparator.comparingInt(Task::getPriority).reversed())
                .toList();
    }

    public List<Task> searchByTitle(String titlePart) {
        return jdbcClientRepo.findByTitleContaining(titlePart);
    }
}
