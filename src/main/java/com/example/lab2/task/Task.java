package com.example.lab2.task;

import com.example.lab2.annotation.Column;
import com.example.lab2.annotation.Id;
import com.example.lab2.annotation.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Table(name = "tasks")
@Data
public class Task {
    @Id(name = "id")
    private Long id;

    @Column(name = "priority")
    @Min(value = 1, message = "Priority must be between 1 and 3")
    @Max(value = 3, message = "Priority must be between 1 and 3")
    private int priority;

    @Column(name = "title")
    @NotBlank(message = "Title is required")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "completed")
    private boolean completed;

    @Column(name = "date")
    @NotNull(message = "Date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @Column(name = "recursive_type")
    private int recursiveType = RecursiveType.NONE.getValue();
}
