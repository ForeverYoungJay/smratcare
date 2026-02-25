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
import com.zhiyangyun.care.crm.service.CrmLeadActionService;
import java.util.List;
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
  public Result<Integer> batchDelete(@RequestBody CrmLeadBatchDeleteRequest request) {
    return Result.ok(leadActionService.batchDelete(AuthContext.getOrgId(), request));
  }

  @PostMapping("/{leadId}/callback-plans")
  public Result<CrmCallbackPlanResponse> createCallbackPlan(
      @PathVariable Long leadId, @RequestBody CrmCallbackPlanRequest request) {
    return Result.ok(leadActionService.createCallbackPlan(
        AuthContext.getOrgId(), AuthContext.getOrgId(), AuthContext.getStaffId(), leadId, request));
  }

  @GetMapping("/{leadId}/callback-plans")
  public Result<List<CrmCallbackPlanResponse>> listCallbackPlan(@PathVariable Long leadId) {
    return Result.ok(leadActionService.listCallbackPlan(AuthContext.getOrgId(), leadId));
  }

  @PostMapping("/callback-plans/{planId}/execute")
  public Result<CrmCallbackPlanResponse> executeCallbackPlan(
      @PathVariable Long planId, @RequestBody(required = false) CrmCallbackPlanExecuteRequest request) {
    return Result.ok(leadActionService.executeCallbackPlan(AuthContext.getOrgId(), planId, request));
  }

  @PostMapping("/{leadId}/attachments")
  public Result<CrmContractAttachmentResponse> createAttachment(
      @PathVariable Long leadId, @RequestBody CrmContractAttachmentRequest request) {
    return Result.ok(leadActionService.createAttachment(
        AuthContext.getOrgId(), AuthContext.getOrgId(), AuthContext.getStaffId(), leadId, request));
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
    return Result.ok(leadActionService.sendSmsTask(AuthContext.getOrgId(), taskId));
  }
}
