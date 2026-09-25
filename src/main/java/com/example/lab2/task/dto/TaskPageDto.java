package com.example.lab2.task.dto;

import java.util.List;

public record TaskPageDto(
        List<TaskDto> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}