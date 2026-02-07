package com.zhiyangyun.care.controller;

import com.zhiyangyun.care.model.CareTaskTodayItem;
import com.zhiyangyun.care.model.ExecuteTaskRequest;
import com.zhiyangyun.care.model.ExecuteTaskResponse;
import com.zhiyangyun.care.service.CareTaskService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/care/tasks")
public class CareTaskController {
  private final CareTaskService careTaskService;

  public CareTaskController(CareTaskService careTaskService) {
    this.careTaskService = careTaskService;
  }

  @GetMapping("/today")
  public List<CareTaskTodayItem> getTodayTasks(@RequestParam(required = false) Long staffId) {
    return careTaskService.getTodayTasks(staffId, LocalDate.now());
  }

  @PostMapping("/execute")
  public ExecuteTaskResponse executeTask(@Valid @RequestBody ExecuteTaskRequest request) {
    return careTaskService.executeTask(request);
  }

  @PostMapping("/generate")
  public int generateTasks(@RequestParam(required = false) String date,
                           @RequestParam(required = false, defaultValue = "false") boolean force) {
    LocalDate target = date == null || date.isBlank()
        ? LocalDate.now()
        : LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
    return careTaskService.generateDailyTasks(target, force);
  }
}
