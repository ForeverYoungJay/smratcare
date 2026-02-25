package com.zhiyangyun.care.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.finance.entity.AdmissionFeeAudit;
import com.zhiyangyun.care.finance.entity.ConsumptionRecord;
import com.zhiyangyun.care.finance.entity.DischargeFeeAudit;
import com.zhiyangyun.care.finance.entity.DischargeSettlement;
import com.zhiyangyun.care.finance.entity.MonthlyAllocation;
import com.zhiyangyun.care.finance.model.AdmissionFeeAuditCreateRequest;
import com.zhiyangyun.care.finance.model.ConsumptionRecordCreateRequest;
import com.zhiyangyun.care.finance.model.DischargeFeeAuditCreateRequest;
import com.zhiyangyun.care.finance.model.DischargeSettlementConfirmRequest;
import com.zhiyangyun.care.finance.model.DischargeSettlementCreateRequest;
import com.zhiyangyun.care.finance.model.FeeAuditReviewRequest;
import com.zhiyangyun.care.finance.model.MonthlyAllocationCreateRequest;
import com.zhiyangyun.care.finance.service.FeeManagementService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance/fee")
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
public class FeeManagementController {
  private final FeeManagementService feeManagementService;
  private final AuditLogService auditLogService;

  public FeeManagementController(FeeManagementService feeManagementService, AuditLogService auditLogService) {
    this.feeManagementService = feeManagementService;
    this.auditLogService = auditLogService;
  }

  @GetMapping("/admission-audit/page")
  public Result<IPage<AdmissionFeeAudit>> admissionAuditPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    return Result.ok(feeManagementService.admissionAuditPage(orgId, pageNo, pageSize, elderId, status, keyword));
  }

  @PostMapping("/admission-audit")
  public Result<AdmissionFeeAudit> createAdmissionAudit(@Valid @RequestBody AdmissionFeeAuditCreateRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long operatorId = AuthContext.getStaffId();
    AdmissionFeeAudit result = feeManagementService.createAdmissionAudit(orgId, operatorId, request);
    auditLogService.record(orgId, orgId, operatorId, AuthContext.getUsername(),
        "FIN_ADMISSION_AUDIT_CREATE", "FINANCE_FEE", result.getId(), "入住费用审核创建");
    return Result.ok(result);
  }

  @PutMapping("/admission-audit/{id}/review")
  @PreAuthorize("hasRole('ADMIN')")
  public Result<AdmissionFeeAudit> reviewAdmissionAudit(@PathVariable Long id,
      @Valid @RequestBody FeeAuditReviewRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long operatorId = AuthContext.getStaffId();
    AdmissionFeeAudit result = feeManagementService.reviewAdmissionAudit(orgId, operatorId, id, request);
    auditLogService.record(orgId, orgId, operatorId, AuthContext.getUsername(),
        "FIN_ADMISSION_AUDIT_REVIEW", "FINANCE_FEE", id, "入住费用审核:" + request.getStatus());
    return Result.ok(result);
  }

  @GetMapping("/discharge-audit/page")
  public Result<IPage<DischargeFeeAudit>> dischargeAuditPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    return Result.ok(feeManagementService.dischargeAuditPage(orgId, pageNo, pageSize, elderId, status, keyword));
  }

  @PostMapping("/discharge-audit")
  public Result<DischargeFeeAudit> createDischargeAudit(@Valid @RequestBody DischargeFeeAuditCreateRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long operatorId = AuthContext.getStaffId();
    DischargeFeeAudit result = feeManagementService.createDischargeAudit(orgId, operatorId, request);
    auditLogService.record(orgId, orgId, operatorId, AuthContext.getUsername(),
        "FIN_DISCHARGE_AUDIT_CREATE", "FINANCE_FEE", result.getId(), "退住费用审核创建");
    return Result.ok(result);
  }

  @PutMapping("/discharge-audit/{id}/review")
  @PreAuthorize("hasRole('ADMIN')")
  public Result<DischargeFeeAudit> reviewDischargeAudit(@PathVariable Long id,
      @Valid @RequestBody FeeAuditReviewRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long operatorId = AuthContext.getStaffId();
    DischargeFeeAudit result = feeManagementService.reviewDischargeAudit(orgId, operatorId, id, request);
    auditLogService.record(orgId, orgId, operatorId, AuthContext.getUsername(),
        "FIN_DISCHARGE_AUDIT_REVIEW", "FINANCE_FEE", id, "退住费用审核:" + request.getStatus());
    return Result.ok(result);
  }

  @GetMapping("/discharge-settlement/page")
  public Result<IPage<DischargeSettlement>> dischargeSettlementPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    return Result.ok(feeManagementService.dischargeSettlementPage(orgId, pageNo, pageSize, elderId, status, keyword));
  }

  @PostMapping("/discharge-settlement")
  public Result<DischargeSettlement> createDischargeSettlement(
      @Valid @RequestBody DischargeSettlementCreateRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long operatorId = AuthContext.getStaffId();
    DischargeSettlement result = feeManagementService.createDischargeSettlement(orgId, operatorId, request);
    auditLogService.record(orgId, orgId, operatorId, AuthContext.getUsername(),
        "FIN_DISCHARGE_SETTLEMENT_CREATE", "FINANCE_FEE", result.getId(), "退住结算单创建");
    return Result.ok(result);
  }

  @PostMapping("/discharge-settlement/{id}/confirm")
  @PreAuthorize("hasRole('ADMIN')")
  public Result<DischargeSettlement> confirmDischargeSettlement(@PathVariable Long id,
      @RequestBody(required = false) DischargeSettlementConfirmRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long operatorId = AuthContext.getStaffId();
    DischargeSettlement result = feeManagementService.confirmDischargeSettlement(orgId, operatorId, id, request);
    auditLogService.record(orgId, orgId, operatorId, AuthContext.getUsername(),
        "FIN_DISCHARGE_SETTLEMENT_CONFIRM", "FINANCE_FEE", id, "退住结算确认");
    return Result.ok(result);
  }

  @GetMapping("/consumption/page")
  public Result<IPage<ConsumptionRecord>> consumptionPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String from,
      @RequestParam(required = false) String to,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    return Result.ok(feeManagementService.consumptionPage(orgId, pageNo, pageSize, elderId, from, to, category, keyword));
  }

  @PostMapping("/consumption")
  public Result<ConsumptionRecord> createConsumption(@Valid @RequestBody ConsumptionRecordCreateRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long operatorId = AuthContext.getStaffId();
    ConsumptionRecord result = feeManagementService.createConsumption(orgId, operatorId, request);
    auditLogService.record(orgId, orgId, operatorId, AuthContext.getUsername(),
        "FIN_CONSUMPTION_CREATE", "FINANCE_FEE", result.getId(), "消费登记");
    return Result.ok(result);
  }

  @GetMapping("/monthly-allocation/page")
  public Result<IPage<MonthlyAllocation>> monthlyAllocationPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String month,
      @RequestParam(required = false) String status) {
    Long orgId = AuthContext.getOrgId();
    return Result.ok(feeManagementService.monthlyAllocationPage(orgId, pageNo, pageSize, month, status));
  }

  @PostMapping("/monthly-allocation")
  public Result<MonthlyAllocation> createMonthlyAllocation(@Valid @RequestBody MonthlyAllocationCreateRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long operatorId = AuthContext.getStaffId();
    MonthlyAllocation result = feeManagementService.createMonthlyAllocation(orgId, operatorId, request);
    auditLogService.record(orgId, orgId, operatorId, AuthContext.getUsername(),
        "FIN_MONTHLY_ALLOCATION_CREATE", "FINANCE_FEE", result.getId(), "月分摊费创建");
    return Result.ok(result);
  }
}
