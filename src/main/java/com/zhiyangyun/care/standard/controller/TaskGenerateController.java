package com.zhiyangyun.care.standard.controller;

import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.standard.model.GenerateTaskRequest;
import com.zhiyangyun.care.standard.service.TaskGenerateService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/standard/tasks")
public class TaskGenerateController {
  private final TaskGenerateService taskGenerateService;
  private final AuditLogService auditLogService;

  public TaskGenerateController(TaskGenerateService taskGenerateService, AuditLogService auditLogService) {
    this.taskGenerateService = taskGenerateService;
    this.auditLogService = auditLogService;
  }

  @PostMapping("/generate")
  public Result<Integer> generate(@Valid @RequestBody GenerateTaskRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    int count = taskGenerateService.generateByElderPackage(
        tenantId, request.getDate(), request.getForce() != null && request.getForce());
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "GENERATE", "CARE_TASK_DAILY", null, "按老人套餐生成任务");
    return Result.ok(count);
  }
}
