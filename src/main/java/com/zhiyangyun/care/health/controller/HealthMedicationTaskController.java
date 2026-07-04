package com.zhiyangyun.care.health.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.health.entity.HealthMedicationTask;
import com.zhiyangyun.care.health.mapper.HealthMedicationTaskMapper;
import com.zhiyangyun.care.health.model.HealthMedicationTaskActionRequest;
import com.zhiyangyun.care.health.service.HealthMedicationTaskService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health/medication/task")
public class HealthMedicationTaskController {
  private final HealthMedicationTaskMapper mapper;
  private final HealthMedicationTaskService taskService;

  public HealthMedicationTaskController(HealthMedicationTaskMapper mapper, HealthMedicationTaskService taskService) {
    this.mapper = mapper;
    this.taskService = taskService;
  }

  @GetMapping("/page")
  public Result<IPage<HealthMedicationTask>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) LocalDate taskDate,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(HealthMedicationTask.class)
        .eq(HealthMedicationTask::getIsDeleted, 0)
        .eq(orgId != null, HealthMedicationTask::getOrgId, orgId)
        .eq(elderId != null, HealthMedicationTask::getElderId, elderId)
        .eq(status != null && !status.isBlank(), HealthMedicationTask::getStatus, status)
        .eq(taskDate != null, HealthMedicationTask::getTaskDate, taskDate);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(HealthMedicationTask::getElderName, keyword)
          .or().like(HealthMedicationTask::getDrugName, keyword));
    }
    wrapper.orderByAsc(HealthMedicationTask::getPlannedTime);
    return Result.ok(mapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping("/generate-today")
  public Result<Boolean> generateToday() {
    taskService.generateTasksForDate(AuthContext.getOrgId(), LocalDate.now());
    return Result.ok(true);
  }

  @PutMapping("/{id}/complete")
  public Result<HealthMedicationTask> complete(
      @PathVariable Long id,
      @Valid @RequestBody(required = false) HealthMedicationTaskActionRequest request) {
    return Result.ok(taskService.completeTask(
        AuthContext.getOrgId(),
        AuthContext.getStaffId(),
        AuthContext.getUsername(),
        id,
        request));
  }

  @PutMapping("/{id}/missed")
  public Result<HealthMedicationTask> missed(
      @PathVariable Long id,
      @Valid @RequestBody(required = false) HealthMedicationTaskActionRequest request) {
    return Result.ok(taskService.markTaskMissed(
        AuthContext.getOrgId(),
        AuthContext.getStaffId(),
        AuthContext.getUsername(),
        id,
        request));
  }
}
