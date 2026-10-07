package com.example.lab2.task.web;

import com.example.lab2.task.Task;
import com.example.lab2.task.TaskPatchService;
import com.example.lab2.task.TaskService;
import com.example.lab2.task.dto.TaskDto;
import com.example.lab2.task.dto.TaskPageDto;
import com.example.lab2.task.dto.TaskRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskRestController {

    private final TaskService taskService;
    private final TaskPatchService taskPatchService;

    public TaskRestController(TaskService taskService, TaskPatchService taskPatchService) {
        this.taskService = taskService;
        this.taskPatchService = taskPatchService;
    }

    @GetMapping("/{id}")
    public TaskDto getById(@PathVariable Long id) {
        Task task = taskService.getTaskById(id);
        return TaskDto.from(task);
    }

    @GetMapping
    public ResponseEntity<TaskPageDto> getAll(
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) Boolean completed,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (page < 0
                || size < 1
                || size > 100
                || (priority != null
                && (priority < 1 || priority > 3))) {
            throw new IllegalArgumentException(
                    "Invalid pagination or filter parameters"
            );
        }

        List<Task> tasks = taskService
                .filterTasks(priority, completed, null)
                .stream()
                .sorted(Comparator.comparing(Task::getId))
                .toList();

        int totalElements = tasks.size();
        long offset = (long) page * size;

        int fromIndex = offset >= totalElements
                ? totalElements
                : (int) offset;

        int toIndex = Math.min(fromIndex + size, totalElements);

        List<TaskDto> content = tasks
                .subList(fromIndex, toIndex)
                .stream()
                .map(TaskDto::from)
                .toList();

        int totalPages = totalElements == 0
                ? 0
                : (totalElements + size - 1) / size;

        TaskPageDto result = new TaskPageDto(content,page, size, totalElements, totalPages);

        return ResponseEntity.ok(result);
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<Void> create(@Valid @RequestBody TaskRequest request) {
        Task task = request.toTask();
        taskService.createTask(task);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    public ResponseEntity<TaskDto> update(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        taskService.getTaskById(id);

        Task task = request.toTask();
        task.setId(id);

        taskService.updateTask(task);

        return ResponseEntity.ok(TaskDto.from(task));
    }

    @PatchMapping(value = "/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<TaskDto> patch(@PathVariable Long id, @RequestBody JsonNode patch) {
        Task updated = taskPatchService.patch(id, patch);

        return ResponseEntity.ok(TaskDto.from(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.getTaskById(id);
        taskService.deleteTask(id);

        return ResponseEntity.noContent().build();
    }
}