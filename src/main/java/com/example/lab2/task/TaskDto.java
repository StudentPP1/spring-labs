package com.example.lab2.task;

import java.time.LocalDate;

public record TaskDto(
        Long id,
        int priority,
        String title,
        String description,
        boolean completed,
        LocalDate date,
        RecursiveType recursiveType
) {

    public static TaskDto from(Task task) {
        return new TaskDto(
                task.getId(),
                task.getPriority(),
                task.getTitle(),
                task.getDescription(),
                task.isCompleted(),
                task.getDate(),
                RecursiveType.fromValue(
                        task.getRecursiveType()
                )
        );
    }
}