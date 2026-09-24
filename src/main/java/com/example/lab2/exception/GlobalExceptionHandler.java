package com.example.lab2.exception;

import com.example.lab2.task.TaskNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)
    public String handleTaskNotFound(TaskNotFoundException ex, HttpServletResponse response, Model model) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        model.addAttribute("status", 404);
        model.addAttribute("title", "Task not found");
        model.addAttribute("message", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Content-Type is not supported"
        );

        problem.setTitle("Unsupported media type");

        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(problem);
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneric(Exception ex, HttpServletResponse response, Model model) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        model.addAttribute("status", 500);
        model.addAttribute("title", "Something went wrong");
        model.addAttribute("message", "An unexpected error occurred. Please try again later.");
        return "error";
    }
}
