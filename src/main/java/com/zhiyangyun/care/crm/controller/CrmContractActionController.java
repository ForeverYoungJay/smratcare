package com.zhiyangyun.care.crm.controller;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.crm.model.action.CrmContractAttachmentRequest;
import com.zhiyangyun.care.crm.model.action.CrmContractAttachmentResponse;
import com.zhiyangyun.care.crm.model.action.CrmSmsTaskCreateRequest;
import com.zhiyangyun.care.crm.model.action.CrmSmsTaskResponse;
import com.zhiyangyun.care.crm.service.CrmContractActionService;
import java.util.List;
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
@RequestMapping("/api/crm/contracts")
@PreAuthorize("hasAnyRole('MARKETING_EMPLOYEE','MARKETING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
public class CrmContractActionController {
  private final CrmContractActionService actionService;

  public CrmContractActionController(CrmContractActionService actionService) {
    this.actionService = actionService;
  }

  @PostMapping("/{contractId}/attachments")
  public Result<CrmContractAttachmentResponse> createAttachment(
      @PathVariable Long contractId,
      @RequestBody CrmContractAttachmentRequest request) {
    return Result.ok(actionService.createAttachment(
        AuthContext.getOrgId(), AuthContext.getOrgId(), AuthContext.getStaffId(), contractId, request));
  }

  @GetMapping("/{contractId}/attachments")
  public Result<List<CrmContractAttachmentResponse>> listAttachment(@PathVariable Long contractId) {
    return Result.ok(actionService.listAttachment(AuthContext.getOrgId(), contractId));
  }

  @DeleteMapping("/attachments/{attachmentId}")
  public Result<Void> deleteAttachment(@PathVariable Long attachmentId) {
    actionService.deleteAttachment(AuthContext.getOrgId(), attachmentId);
    return Result.ok(null);
  }

  @PostMapping("/sms/tasks")
  public Result<List<CrmSmsTaskResponse>> createSmsTasks(@RequestBody CrmSmsTaskCreateRequest request) {
    return Result.ok(actionService.createSmsTasks(
        AuthContext.getOrgId(), AuthContext.getOrgId(), AuthContext.getStaffId(), request));
  }

  @GetMapping("/sms/tasks")
  public Result<List<CrmSmsTaskResponse>> listSmsTasks(@RequestParam(required = false) Long contractId) {
    return Result.ok(actionService.listSmsTasks(AuthContext.getOrgId(), contractId));
  }

  @PostMapping("/sms/tasks/{taskId}/send")
  public Result<CrmSmsTaskResponse> sendSmsTask(@PathVariable Long taskId) {
    return Result.ok(actionService.sendSmsTask(AuthContext.getOrgId(), taskId));
  }
}
