package com.example.lab2.task;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title is too long")
        String title,

        String description,

        @Min(value = 1, message = "Priority must be between 1 and 3")
        @Max(value = 3, message = "Priority must be between 1 and 3")
        int priority,

        @NotNull(message = "Completed status is required")
        Boolean completed,

        @NotNull(message = "Date is required")
        LocalDate date,

        @NotNull(message = "Recursive type is required")
        RecursiveType recursiveType
) {

    public Task toTask() {
        Task task = new Task();

        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority);
        task.setCompleted(completed);
        task.setDate(date);
        task.setRecursiveType(recursiveType.getValue());

        return task;
    }

    public static TaskRequest from(Task task) {
        return new TaskRequest(
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.isCompleted(),
                task.getDate(),
                RecursiveType.fromValue(task.getRecursiveType())
        );
    }
}