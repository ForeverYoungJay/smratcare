package com.zhiyangyun.care.crm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.crm.model.CrmContractRequest;
import com.zhiyangyun.care.crm.model.CrmContractResponse;
import com.zhiyangyun.care.crm.model.action.CrmContractBatchDeleteRequest;
import com.zhiyangyun.care.crm.model.action.CrmContractFinalizeRequest;
import com.zhiyangyun.care.crm.service.CrmContractService;
import java.util.List;
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
  private final CrmContractService contractService;

  public CrmContractController(CrmContractService contractService) {
    this.contractService = contractService;
  }

  @PostMapping
  public Result<CrmContractResponse> create(@RequestBody CrmContractRequest request) {
    return Result.ok(contractService.create(
        AuthContext.getOrgId(), AuthContext.getOrgId(), AuthContext.getStaffId(), request));
  }

  @PutMapping("/{id}")
  public Result<CrmContractResponse> update(@PathVariable Long id, @RequestBody CrmContractRequest request) {
    return Result.ok(contractService.update(AuthContext.getOrgId(), AuthContext.getOrgId(), id, request));
  }

  @GetMapping("/{id}")
  public Result<CrmContractResponse> get(@PathVariable Long id) {
    return Result.ok(contractService.get(AuthContext.getOrgId(), id));
  }

  @GetMapping("/page")
  public Result<IPage<CrmContractResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String contractNo,
      @RequestParam(required = false) String elderName,
      @RequestParam(required = false) String elderPhone,
      @RequestParam(required = false) String marketerName,
      @RequestParam(required = false) String orgName,
      @RequestParam(required = false) String flowStage,
      @RequestParam(required = false) String contractStatus,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Boolean overdueOnly,
      @RequestParam(required = false) Boolean sortByOverdue,
      @RequestParam(required = false) String currentOwnerDept) {
    return Result.ok(contractService.page(
        AuthContext.getOrgId(),
        pageNo,
        pageSize,
        keyword,
        contractNo,
        elderName,
        elderPhone,
        marketerName,
        orgName,
        flowStage,
        contractStatus,
        status,
        overdueOnly,
        sortByOverdue,
        currentOwnerDept));
  }

  @PostMapping("/batch/delete")
  public Result<Integer> batchDelete(@RequestBody(required = false) CrmContractBatchDeleteRequest request) {
    return Result.ok(contractService.deleteBatch(
        AuthContext.getOrgId(),
        request == null ? null : request.getIds(),
        request == null ? null : request.getContractNos()));
  }

  @PostMapping("/{id}/handoff-assessment")
  public Result<CrmContractResponse> handoffAssessment(@PathVariable Long id) {
    return Result.ok(contractService.handoffToAssessment(AuthContext.getOrgId(), id));
  }

  @PostMapping("/{id}/approve")
  public Result<CrmContractResponse> approve(
      @PathVariable Long id, @RequestBody(required = false) CrmContractFinalizeRequest request) {
    return Result.ok(contractService.approve(AuthContext.getOrgId(), id, request == null ? null : request.getRemark()));
  }

  @PostMapping("/{id}/reject")
  public Result<CrmContractResponse> reject(
      @PathVariable Long id, @RequestBody(required = false) CrmContractFinalizeRequest request) {
    return Result.ok(contractService.reject(AuthContext.getOrgId(), id, request == null ? null : request.getRemark()));
  }

  @PostMapping("/{id}/void")
  public Result<CrmContractResponse> voidContract(
      @PathVariable Long id, @RequestBody(required = false) CrmContractFinalizeRequest request) {
    return Result.ok(contractService.voidContract(AuthContext.getOrgId(), id, request == null ? null : request.getRemark()));
  }

  @PostMapping("/{id}/finalize")
  public Result<CrmContractResponse> finalizeContract(
      @PathVariable Long id, @RequestBody(required = false) CrmContractFinalizeRequest request) {
    String remark = request == null ? null : request.getRemark();
    return Result.ok(contractService.finalizeSign(AuthContext.getOrgId(), id, remark));
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    contractService.deleteBatch(AuthContext.getOrgId(), List.of(id), null);
    return Result.ok(null);
  }
}
