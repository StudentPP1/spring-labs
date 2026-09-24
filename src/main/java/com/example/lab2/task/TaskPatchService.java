package com.example.lab2.task;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.Set;

@Service
public class TaskPatchService {

    private static final Set<String> ALLOWED_FIELDS = Set.of(
            "title",
            "description",
            "priority",
            "completed",
            "date",
            "recursiveType"
    );

    private final TaskService taskService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public TaskPatchService(TaskService taskService, ObjectMapper objectMapper, Validator validator) {
        this.taskService = taskService;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @Transactional
    public Task patch(Long id, JsonNode patch) {
        if (patch == null || !patch.isObject()) {
            throw new IllegalArgumentException("Merge patch must be a JSON object");
        }

        validateFields(patch);

        Task current = taskService.getTaskById(id);

        ObjectNode merged = objectMapper
                .valueToTree(TaskRequest.from(current));

        patch.properties().forEach(entry -> {
            String field = entry.getKey();
            JsonNode value = entry.getValue();

            if (value.isNull()) {
                merged.remove(field);
            } else {
                merged.set(field, value);
            }
        });

        TaskRequest request;

        try {
            request = objectMapper.treeToValue(
                    merged,
                    TaskRequest.class
            );
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(
                    "Patch contains invalid field values"
            );
        }

        var violations = validator.validate(request);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        Task updated = request.toTask();
        updated.setId(id);

        taskService.updateTask(updated);

        return updated;
    }

    private void validateFields(JsonNode patch) {
        patch.properties().forEach(entry -> {
            String field = entry.getKey();
            JsonNode value = entry.getValue();

            if (!ALLOWED_FIELDS.contains(field)) {
                throw new IllegalArgumentException(
                        "Unknown or read-only field: " + field
                );
            }

            if (value.isNull()) {
                return;
            }

            boolean validType = switch (field) {
                case "priority" ->
                        value.isIntegralNumber()
                                && value.canConvertToInt();

                case "completed" ->
                        value.isBoolean();

                default ->
                        value.isString();
            };

            if (!validType) {
                throw new IllegalArgumentException(
                        "Invalid JSON type for field: " + field
                );
            }
        });
    }
}