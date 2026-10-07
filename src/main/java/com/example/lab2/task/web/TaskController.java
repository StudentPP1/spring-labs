package com.example.lab2.task.web;

import com.example.lab2.task.RecursiveType;
import com.example.lab2.task.Task;
import com.example.lab2.task.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Controller
public class TaskController {

    private static final List<String> WEEK_DAY_NAMES = List.of(
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");

    @Autowired
    private TaskService taskService;

    @GetMapping("/")
    public String home() {
        return "redirect:/tasks/week";
    }

    @GetMapping("/tasks")
    public String list(@RequestParam(required = false) String sort,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) Integer priority,
                        Model model) {
        Boolean completed = switch (status == null ? "" : status) {
            case "active" -> false;
            case "completed" -> true;
            default -> null;
        };
        List<Task> tasks = taskService.filterTasks(priority, completed, sort);
        model.addAttribute("tasks", tasks);
        model.addAttribute("sort", sort);
        model.addAttribute("status", status);
        model.addAttribute("priority", priority);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("totalCount", tasks.size());
        model.addAttribute("completedCount", tasks.stream().filter(Task::isCompleted).count());
        return "tasks/list";
    }

    @GetMapping("/tasks/week")
    public String week(@RequestParam(required = false)
                       @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate start,
                       Model model) {
        LocalDate anchor = start != null ? start : LocalDate.now();
        LocalDate weekStart = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        List<LocalDate> weekDates = IntStream.range(0, 7)
                .mapToObj(weekStart::plusDays)
                .toList();

        Map<LocalDate, List<Task>> tasksByDate = new LinkedHashMap<>();
        for (LocalDate date : weekDates) {
            tasksByDate.put(date, new ArrayList<>());
        }
        for (Task task : taskService.getTasksBetween(weekStart, weekEnd)) {
            tasksByDate.get(task.getDate()).add(task);
        }

        model.addAttribute("weekDates", weekDates);
        model.addAttribute("weekDayNames", WEEK_DAY_NAMES);
        model.addAttribute("tasksByDate", tasksByDate);
        model.addAttribute("weekStart", weekStart);
        model.addAttribute("weekEnd", weekEnd);
        model.addAttribute("prevWeek", weekStart.minusWeeks(1));
        model.addAttribute("nextWeek", weekStart.plusWeeks(1));
        model.addAttribute("currentWeekStart", LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)));
        model.addAttribute("today", LocalDate.now());
        return "tasks/week";
    }

    @GetMapping("/tasks/new")
    public String createForm(Model model) {
        model.addAttribute("task", new Task());
        model.addAttribute("recursiveTypes", RecursiveType.values());
        return "tasks/form";
    }

    @PostMapping("/tasks")
    public String create(@Valid @ModelAttribute("task") Task task, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("recursiveTypes", RecursiveType.values());
            return "tasks/form";
        }
        taskService.createTask(task);
        return "redirect:/tasks";
    }

    @GetMapping("/tasks/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("task", taskService.getTaskById(id));
        model.addAttribute("recursiveTypes", RecursiveType.values());
        return "tasks/form";
    }

    @PostMapping("/tasks/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("task") Task task,
                         BindingResult bindingResult, Model model) {
        task.setId(id);
        if (bindingResult.hasErrors()) {
            model.addAttribute("recursiveTypes", RecursiveType.values());
            return "tasks/form";
        }
        taskService.updateTask(task);
        return "redirect:/tasks";
    }

    @PostMapping("/tasks/{id}/delete")
    public String delete(@PathVariable Long id) {
        taskService.deleteTask(id);
        return "redirect:/tasks";
    }

    @PostMapping("/tasks/{id}/complete")
    public String complete(@PathVariable Long id) {
        taskService.markAsCompleted(id);
        return "redirect:/tasks";
    }
}
