package com.zhiyangyun.care.crm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.crm.model.CrmContractRequest;
import com.zhiyangyun.care.crm.model.CrmContractResponse;
import com.zhiyangyun.care.crm.model.CrmContractStageSummaryResponse;
import com.zhiyangyun.care.crm.model.action.CrmContractBatchDeleteRequest;
import com.zhiyangyun.care.crm.model.action.CrmContractFinalizeRequest;
import com.zhiyangyun.care.crm.service.CrmContractService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crm/contracts")
public class CrmContractController {
  private static final String CRM_CONTRACT_READ =
      "hasAnyRole('STAFF','HR_EMPLOYEE','HR_MINISTER','MEDICAL_EMPLOYEE','MEDICAL_MINISTER',"
          + "'NURSING_EMPLOYEE','NURSING_MINISTER','FINANCE_EMPLOYEE','FINANCE_MINISTER',"
          + "'LOGISTICS_EMPLOYEE','LOGISTICS_MINISTER','MARKETING_EMPLOYEE','MARKETING_MINISTER',"
          + "'DIRECTOR','SYS_ADMIN','ADMIN')";
  private static final String CRM_CONTRACT_WRITE =
      "hasAnyRole('MARKETING_EMPLOYEE','MARKETING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";
  private final CrmContractService contractService;

  public CrmContractController(CrmContractService contractService) {
    this.contractService = contractService;
  }

  @PreAuthorize(CRM_CONTRACT_WRITE)
  @PostMapping
  public Result<CrmContractResponse> create(@RequestBody CrmContractRequest request) {
    return Result.ok(contractService.create(
        AuthContext.getOrgId(), AuthContext.getOrgId(), AuthContext.getStaffId(), request));
  }

  @PreAuthorize(CRM_CONTRACT_WRITE)
  @PutMapping("/{id}")
  public Result<CrmContractResponse> update(@PathVariable Long id, @RequestBody CrmContractRequest request) {
    return Result.ok(contractService.update(AuthContext.getOrgId(), AuthContext.getOrgId(), id, request));
  }

  @PreAuthorize(CRM_CONTRACT_READ)
  @GetMapping("/{id}")
  public Result<CrmContractResponse> get(@PathVariable Long id) {
    return Result.ok(contractService.get(AuthContext.getOrgId(), id));
  }

  @PreAuthorize(CRM_CONTRACT_READ)
  @GetMapping("/page")
  public Result<IPage<CrmContractResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String contractNo,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String elderName,
      @RequestParam(required = false) String elderPhone,
      @RequestParam(required = false) String marketerName,
      @RequestParam(required = false) String orgName,
      @RequestParam(required = false) String flowStage,
      @RequestParam(required = false) String contractStatus,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String changeWorkflowStatus,
      @RequestParam(required = false) Boolean excludeSigned,
      @RequestParam(required = false) Boolean overdueOnly,
      @RequestParam(required = false) Boolean sortByOverdue,
      @RequestParam(required = false) String currentOwnerDept) {
    return Result.ok(contractService.page(
        AuthContext.getOrgId(),
        pageNo,
        pageSize,
        keyword,
        contractNo,
        elderId,
        elderName,
        elderPhone,
        marketerName,
        orgName,
        flowStage,
        contractStatus,
        status,
        changeWorkflowStatus,
        excludeSigned,
        overdueOnly,
        sortByOverdue,
        currentOwnerDept));
  }

  @PreAuthorize(CRM_CONTRACT_READ)
  @GetMapping("/stage-summary")
  public Result<CrmContractStageSummaryResponse> stageSummary(
      @RequestParam(required = false) String currentOwnerDept) {
    Long orgId = AuthContext.getOrgId();
    CrmContractStageSummaryResponse response = new CrmContractStageSummaryResponse();
    response.setPendingAssessment(resolveStageTotal(orgId, "PENDING_ASSESSMENT", false, currentOwnerDept));
    response.setPendingBedSelect(resolveStageTotal(orgId, "PENDING_BED_SELECT", false, currentOwnerDept));
    response.setPendingSign(resolveStageTotal(orgId, "PENDING_SIGN", false, currentOwnerDept));
    response.setSigned(resolveStageTotal(orgId, "SIGNED", false, currentOwnerDept));
    response.setPendingAssessmentOverdue(resolveStageTotal(orgId, "PENDING_ASSESSMENT", true, currentOwnerDept));
    response.setPendingSignOverdue(resolveStageTotal(orgId, "PENDING_SIGN", true, currentOwnerDept));
    response.setGeneratedAt(LocalDateTime.now());
    return Result.ok(response);
  }

  private long resolveStageTotal(
      Long orgId,
      String flowStage,
      boolean overdueOnly,
      String currentOwnerDept) {
    return contractService.page(
            orgId,
            1,
            1,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            flowStage,
            null,
            null,
            null,
            false,
            overdueOnly,
            false,
            currentOwnerDept)
        .getTotal();
  }

  @PreAuthorize(CRM_CONTRACT_WRITE)
  @PostMapping("/batch/delete")
  public Result<Integer> batchDelete(@RequestBody(required = false) CrmContractBatchDeleteRequest request) {
    return Result.ok(contractService.deleteBatch(
        AuthContext.getOrgId(),
        request == null ? null : request.getIds(),
        request == null ? null : request.getContractNos()));
  }

  @PreAuthorize(CRM_CONTRACT_WRITE)
  @PostMapping("/{id}/handoff-assessment")
  public Result<CrmContractResponse> handoffAssessment(@PathVariable Long id) {
    return Result.ok(contractService.handoffToAssessment(AuthContext.getOrgId(), id));
  }

  @PreAuthorize(CRM_CONTRACT_WRITE)
  @PostMapping("/{id}/move-to-bed-select")
  public Result<CrmContractResponse> moveToBedSelect(@PathVariable Long id) {
    return Result.ok(contractService.moveToBedSelect(AuthContext.getOrgId(), id));
  }

  @PreAuthorize(CRM_CONTRACT_WRITE)
  @PostMapping("/{id}/move-to-pending-sign")
  public Result<CrmContractResponse> moveToPendingSign(@PathVariable Long id) {
    return Result.ok(contractService.moveToPendingSign(AuthContext.getOrgId(), id));
  }

  @PreAuthorize(CRM_CONTRACT_WRITE)
  @PostMapping("/{id}/change/start")
  public Result<CrmContractResponse> startChange(
      @PathVariable Long id, @RequestBody(required = false) CrmContractFinalizeRequest request) {
    return Result.ok(contractService.startChange(AuthContext.getOrgId(), id, request == null ? null : request.getRemark()));
  }

  @PreAuthorize(CRM_CONTRACT_WRITE)
  @PostMapping("/{id}/change/submit")
  public Result<CrmContractResponse> submitChange(
      @PathVariable Long id, @RequestBody(required = false) CrmContractFinalizeRequest request) {
    return Result.ok(contractService.submitChange(AuthContext.getOrgId(), id, request == null ? null : request.getRemark()));
  }

  @PreAuthorize(CRM_CONTRACT_WRITE)
  @PostMapping("/{id}/change/approve")
  public Result<CrmContractResponse> approveChange(
      @PathVariable Long id, @RequestBody(required = false) CrmContractFinalizeRequest request) {
    return Result.ok(contractService.approveChange(AuthContext.getOrgId(), id, request == null ? null : request.getRemark()));
  }

  @PreAuthorize(CRM_CONTRACT_WRITE)
  @PostMapping("/{id}/change/reject")
  public Result<CrmContractResponse> rejectChange(
      @PathVariable Long id, @RequestBody(required = false) CrmContractFinalizeRequest request) {
    return Result.ok(contractService.rejectChange(AuthContext.getOrgId(), id, request == null ? null : request.getRemark()));
  }

  @PreAuthorize(CRM_CONTRACT_WRITE)
  @PostMapping("/{id}/approve")
  public Result<CrmContractResponse> approve(
      @PathVariable Long id, @RequestBody(required = false) CrmContractFinalizeRequest request) {
    return Result.ok(contractService.approve(AuthContext.getOrgId(), id, request == null ? null : request.getRemark()));
  }

  @PreAuthorize(CRM_CONTRACT_WRITE)
  @PostMapping("/{id}/reject")
  public Result<CrmContractResponse> reject(
      @PathVariable Long id, @RequestBody(required = false) CrmContractFinalizeRequest request) {
    return Result.ok(contractService.reject(AuthContext.getOrgId(), id, request == null ? null : request.getRemark()));
  }

  @PreAuthorize(CRM_CONTRACT_WRITE)
  @PostMapping("/{id}/void")
  public Result<CrmContractResponse> voidContract(
      @PathVariable Long id, @RequestBody(required = false) CrmContractFinalizeRequest request) {
    return Result.ok(contractService.voidContract(AuthContext.getOrgId(), id, request == null ? null : request.getRemark()));
  }

  @PreAuthorize(CRM_CONTRACT_WRITE)
  @PostMapping("/{id}/finalize")
  public Result<CrmContractResponse> finalizeContract(
      @PathVariable Long id, @RequestBody(required = false) CrmContractFinalizeRequest request) {
    String remark = request == null ? null : request.getRemark();
    return Result.ok(contractService.finalizeSign(AuthContext.getOrgId(), AuthContext.getStaffId(), id, remark));
  }

  @PreAuthorize(CRM_CONTRACT_WRITE)
  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    contractService.deleteBatch(AuthContext.getOrgId(), List.of(id), null);
    return Result.ok(null);
  }
}
