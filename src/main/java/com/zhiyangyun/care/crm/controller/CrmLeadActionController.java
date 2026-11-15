package com.zhiyangyun.care.crm.controller;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.crm.model.action.CrmCallbackPlanExecuteRequest;
import com.zhiyangyun.care.crm.model.action.CrmCallbackPlanRequest;
import com.zhiyangyun.care.crm.model.action.CrmCallbackPlanResponse;
import com.zhiyangyun.care.crm.model.action.CrmContractAttachmentRequest;
import com.zhiyangyun.care.crm.model.action.CrmContractAttachmentResponse;
import com.zhiyangyun.care.crm.model.action.CrmLeadBatchDeleteRequest;
import com.zhiyangyun.care.crm.model.action.CrmLeadBatchStatusRequest;
import com.zhiyangyun.care.crm.model.action.CrmSmsTaskCreateRequest;
import com.zhiyangyun.care.crm.model.action.CrmSmsTaskResponse;
import com.zhiyangyun.care.crm.model.CrmLeadResponse;
import com.zhiyangyun.care.crm.service.CrmLeadActionService;
import com.zhiyangyun.care.common.web.ResourceNotFoundException;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crm/leads")
@PreAuthorize("hasAnyRole('MARKETING_EMPLOYEE','MARKETING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
public class CrmLeadActionController {
  private final CrmLeadActionService leadActionService;

  public CrmLeadActionController(CrmLeadActionService leadActionService) {
    this.leadActionService = leadActionService;
  }

  @PostMapping("/batch/status")
  public Result<Integer> batchStatus(@RequestBody CrmLeadBatchStatusRequest request) {
    return Result.ok(leadActionService.batchUpdateStatus(AuthContext.getOrgId(), request));
  }

  @PostMapping("/batch/delete")
  @PreAuthorize("hasAnyRole('MARKETING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  public Result<Integer> batchDelete(@RequestBody CrmLeadBatchDeleteRequest request) {
    return Result.ok(leadActionService.batchDelete(AuthContext.getOrgId(), request));
  }

  @PostMapping("/contract/{contractNo}/handoff-assessment")
  public Result<CrmLeadResponse> handoffAssessment(@PathVariable String contractNo) {
    return Result.ok(requireFound(leadActionService.handoffToAssessment(AuthContext.getOrgId(), contractNo), "客户线索"));
  }

  @PostMapping("/{leadId}/callback-plans")
  public Result<CrmCallbackPlanResponse> createCallbackPlan(
      @PathVariable Long leadId, @RequestBody CrmCallbackPlanRequest request) {
    return Result.ok(requireFound(leadActionService.createCallbackPlan(
        AuthContext.getOrgId(), AuthContext.getOrgId(), AuthContext.getStaffId(), leadId, request), "客户线索"));
  }

  @GetMapping("/{leadId}/callback-plans")
  public Result<List<CrmCallbackPlanResponse>> listCallbackPlan(@PathVariable Long leadId) {
    return Result.ok(leadActionService.listCallbackPlan(AuthContext.getOrgId(), leadId));
  }

  @GetMapping("/callback-plans/batch")
  public Result<Map<Long, List<CrmCallbackPlanResponse>>> listCallbackPlans(
      @RequestParam List<Long> leadIds) {
    return Result.ok(leadActionService.listCallbackPlans(AuthContext.getOrgId(), leadIds));
  }

  @PostMapping("/callback-plans/{planId}/execute")
  public Result<CrmCallbackPlanResponse> executeCallbackPlan(
      @PathVariable Long planId, @RequestBody(required = false) CrmCallbackPlanExecuteRequest request) {
    return Result.ok(requireFound(leadActionService.executeCallbackPlan(AuthContext.getOrgId(), planId, request), "回访计划"));
  }

  @PostMapping("/{leadId}/attachments")
  public Result<CrmContractAttachmentResponse> createAttachment(
      @PathVariable Long leadId, @RequestBody CrmContractAttachmentRequest request) {
    return Result.ok(requireFound(leadActionService.createAttachment(
        AuthContext.getOrgId(), AuthContext.getOrgId(), AuthContext.getStaffId(), leadId, request), "客户线索"));
  }

  @GetMapping("/{leadId}/attachments")
  public Result<List<CrmContractAttachmentResponse>> listAttachment(@PathVariable Long leadId) {
    return Result.ok(leadActionService.listAttachment(AuthContext.getOrgId(), leadId));
  }

  @DeleteMapping("/attachments/{attachmentId}")
  public Result<Void> deleteAttachment(@PathVariable Long attachmentId) {
    leadActionService.deleteAttachment(AuthContext.getOrgId(), attachmentId);
    return Result.ok(null);
  }

  @PostMapping("/sms/tasks")
  public Result<List<CrmSmsTaskResponse>> createSmsTasks(@RequestBody CrmSmsTaskCreateRequest request) {
    return Result.ok(leadActionService.createSmsTasks(
        AuthContext.getOrgId(), AuthContext.getOrgId(), AuthContext.getStaffId(), request));
  }

  @GetMapping("/sms/tasks")
  public Result<List<CrmSmsTaskResponse>> listSmsTasks(@RequestParam(required = false) Long leadId) {
    return Result.ok(leadActionService.listSmsTasks(AuthContext.getOrgId(), leadId));
  }

  @PostMapping("/sms/tasks/{taskId}/send")
  public Result<CrmSmsTaskResponse> sendSmsTask(@PathVariable Long taskId) {
    return Result.ok(requireFound(leadActionService.sendSmsTask(AuthContext.getOrgId(), taskId), "短信任务"));
  }

  private <T> T requireFound(T value, String resourceName) {
    if (value == null) {
      throw new ResourceNotFoundException(resourceName + "不存在或无权访问");
    }
    return value;
  }
}
