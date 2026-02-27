package com.zhiyangyun.care.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.model.CareTaskAssignRequest;
import com.zhiyangyun.care.model.CareTaskBatchAssignRequest;
import com.zhiyangyun.care.model.CareTaskCreateRequest;
import com.zhiyangyun.care.model.CareTaskExecuteLogItem;
import com.zhiyangyun.care.model.CareTaskReviewRequest;
import com.zhiyangyun.care.model.CareTaskSummaryResponse;
import com.zhiyangyun.care.model.CareTaskTodayItem;
import com.zhiyangyun.care.model.ExecuteTaskRequest;
import com.zhiyangyun.care.model.ExecuteTaskResponse;
import com.zhiyangyun.care.service.CareTaskService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
  public List<CareTaskTodayItem> getTodayTasks(
      @RequestParam(required = false) Long staffId,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String date) {
    LocalDate target = date == null || date.isBlank()
        ? LocalDate.now()
        : LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
    return careTaskService.getTodayTasks(AuthContext.getOrgId(), staffId, elderId, target);
  }

  @GetMapping("/logs")
  public Result<List<CareTaskExecuteLogItem>> logs(@RequestParam Long taskDailyId) {
    return Result.ok(careTaskService.listExecuteLogs(AuthContext.getOrgId(), taskDailyId));
  }

  @GetMapping("/page")
  public Result<IPage<CareTaskTodayItem>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String date,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo,
      @RequestParam(required = false) Long staffId,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String roomNo,
      @RequestParam(required = false) String careLevel,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Boolean overdueOnly) {
    LocalDate parsedDate = parseLocalDate(date);
    LocalDate from = parseLocalDate(dateFrom);
    LocalDate to = parseLocalDate(dateTo);
    if (parsedDate != null) {
      from = parsedDate;
      to = parsedDate;
    }
    return Result.ok(careTaskService.page(AuthContext.getOrgId(), pageNo, pageSize, from, to, staffId, elderId,
        roomNo, careLevel, status, keyword, overdueOnly));
  }

  @GetMapping("/summary")
  public Result<CareTaskSummaryResponse> summary(
      @RequestParam(required = false) String date,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo,
      @RequestParam(required = false) Long staffId,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String roomNo,
      @RequestParam(required = false) String careLevel,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Boolean overdueOnly) {
    LocalDate parsedDate = parseLocalDate(date);
    LocalDate from = parseLocalDate(dateFrom);
    LocalDate to = parseLocalDate(dateTo);
    if (parsedDate != null) {
      from = parsedDate;
      to = parsedDate;
    }
    return Result.ok(careTaskService.summary(AuthContext.getOrgId(), from, to, staffId, elderId, roomNo, careLevel,
        status, keyword, overdueOnly));
  }

  @PostMapping("/execute")
  public ExecuteTaskResponse executeTask(@Valid @RequestBody ExecuteTaskRequest request) {
    return careTaskService.executeTask(AuthContext.getOrgId(), request);
  }

  @PostMapping("/generate")
  public int generateTasks(@RequestParam(required = false) String date,
                           @RequestParam(required = false, defaultValue = "false") boolean force) {
    LocalDate target = date == null || date.isBlank()
        ? LocalDate.now()
        : LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
    return careTaskService.generateDailyTasks(AuthContext.getOrgId(), target, force);
  }

  @PostMapping("/assign")
  public Result<Void> assign(@Valid @RequestBody CareTaskAssignRequest request) {
    careTaskService.assignTask(AuthContext.getOrgId(), request.getTaskDailyId(), request.getStaffId(),
        request.getForce() != null && request.getForce());
    return Result.ok(null);
  }

  @PostMapping("/assign/batch")
  public Result<Integer> assignBatch(@RequestBody CareTaskBatchAssignRequest request) {
    int updated = careTaskService.assignBatch(
        AuthContext.getOrgId(),
        request.getDateFrom(),
        request.getDateTo(),
        request.getStaffId(),
        request.getRoomNo(),
        request.getFloorNo(),
        request.getBuilding(),
        request.getCareLevel(),
        request.getStatus(),
        request.getForce() != null && request.getForce());
    return Result.ok(updated);
  }

  @PostMapping("/review")
  public Result<Void> review(@Valid @RequestBody CareTaskReviewRequest request) {
    LocalDateTime reviewTime = request.getReviewTime() == null || request.getReviewTime().isBlank()
        ? LocalDateTime.now()
        : LocalDateTime.parse(request.getReviewTime(), DateTimeFormatter.ISO_DATE_TIME);
    careTaskService.reviewTask(
        AuthContext.getOrgId(),
        request.getTaskDailyId(),
        request.getStaffId(),
        request.getScore(),
        request.getComment(),
        request.getReviewerType(),
        AuthContext.getStaffId(),
        AuthContext.getUsername(),
        reviewTime);
    return Result.ok(null);
  }

  @PostMapping("/create")
  public Result<Long> create(@Valid @RequestBody CareTaskCreateRequest request) {
    Long id = careTaskService.createTask(AuthContext.getOrgId(), request);
    return Result.ok(id);
  }

  private LocalDate parseLocalDate(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return LocalDate.parse(value, DateTimeFormatter.ISO_DATE);
  }
}
