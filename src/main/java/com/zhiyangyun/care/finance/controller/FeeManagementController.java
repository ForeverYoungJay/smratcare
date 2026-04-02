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
import com.zhiyangyun.care.finance.mapper.DischargeSettlementMapper;
import com.zhiyangyun.care.finance.model.AdmissionFeeAuditCreateRequest;
import com.zhiyangyun.care.finance.model.ConsumptionRecordCreateRequest;
import com.zhiyangyun.care.finance.model.DischargeFeeAuditCreateRequest;
import com.zhiyangyun.care.finance.model.DischargeSettlementConfirmRequest;
import com.zhiyangyun.care.finance.model.DischargeSettlementCreateRequest;
import com.zhiyangyun.care.finance.model.FeeAuditReviewRequest;
import com.zhiyangyun.care.finance.model.MonthlyAllocationCreateRequest;
import com.zhiyangyun.care.finance.model.MonthlyAllocationPreviewRequest;
import com.zhiyangyun.care.finance.model.MonthlyAllocationPreviewResponse;
import com.zhiyangyun.care.finance.model.MonthlyAllocationRollbackRequest;
import com.zhiyangyun.care.finance.service.FeeManagementService;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
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
@PreAuthorize("hasAnyRole('FINANCE_EMPLOYEE','FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
public class FeeManagementController {
  private final FeeManagementService feeManagementService;
  private final AuditLogService auditLogService;
  private final DischargeSettlementMapper dischargeSettlementMapper;

  public FeeManagementController(FeeManagementService feeManagementService, AuditLogService auditLogService,
      DischargeSettlementMapper dischargeSettlementMapper) {
    this.feeManagementService = feeManagementService;
    this.auditLogService = auditLogService;
    this.dischargeSettlementMapper = dischargeSettlementMapper;
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
    auditLogService.recordStructured(
        orgId, orgId, operatorId, AuthContext.getUsername(),
        "FIN_ADMISSION_AUDIT_CREATE", "FINANCE_FEE", result.getId(), "入住费用审核创建",
        null, result, request);
    return Result.ok(result);
  }

  @PutMapping("/admission-audit/{id}/review")
  @PreAuthorize("hasAnyRole('FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  public Result<AdmissionFeeAudit> reviewAdmissionAudit(@PathVariable Long id,
      @Valid @RequestBody FeeAuditReviewRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long operatorId = AuthContext.getStaffId();
    AdmissionFeeAudit result = feeManagementService.reviewAdmissionAudit(orgId, operatorId, id, request);
    auditLogService.recordStructured(
        orgId, orgId, operatorId, AuthContext.getUsername(),
        "FIN_ADMISSION_AUDIT_REVIEW", "FINANCE_FEE", id, "入住费用审核:" + request.getStatus(),
        null, result, request);
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
    auditLogService.recordStructured(
        orgId, orgId, operatorId, AuthContext.getUsername(),
        "FIN_DISCHARGE_AUDIT_CREATE", "FINANCE_FEE", result.getId(), "退住费用审核创建",
        null, result, request);
    return Result.ok(result);
  }

  @PutMapping("/discharge-audit/{id}/review")
  @PreAuthorize("hasAnyRole('FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  public Result<DischargeFeeAudit> reviewDischargeAudit(@PathVariable Long id,
      @Valid @RequestBody FeeAuditReviewRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long operatorId = AuthContext.getStaffId();
    DischargeFeeAudit result = feeManagementService.reviewDischargeAudit(orgId, operatorId, id, request);
    auditLogService.recordStructured(
        orgId, orgId, operatorId, AuthContext.getUsername(),
        "FIN_DISCHARGE_AUDIT_REVIEW", "FINANCE_FEE", id, "退住费用审核:" + request.getStatus(),
        null, result, request);
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
    Map<String, Object> context = new LinkedHashMap<>();
    context.put("elderId", request.getElderId());
    context.put("dischargeApplyId", request.getDischargeApplyId());
    context.put("payableAmount", request.getPayableAmount());
    context.put("feeItem", request.getFeeItem());
    context.put("dischargeFeeConfig", request.getDischargeFeeConfig());
    context.put("remark", request.getRemark());
    auditLogService.recordStructured(
        orgId, orgId, operatorId, AuthContext.getUsername(),
        "FIN_DISCHARGE_SETTLEMENT_CREATE", "FINANCE_FEE", result.getId(), "退住结算单创建",
        null, result, context);
    return Result.ok(result);
  }

  @PostMapping("/discharge-settlement/{id}/confirm")
  @PreAuthorize("hasAnyRole('FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  public Result<DischargeSettlement> confirmDischargeSettlement(@PathVariable Long id,
      @RequestBody(required = false) DischargeSettlementConfirmRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long operatorId = AuthContext.getStaffId();
    DischargeSettlement beforeSnapshot = dischargeSettlementMapper.selectById(id);
    DischargeSettlement result = feeManagementService.confirmDischargeSettlement(orgId, operatorId, id, request);
    Map<String, Object> context = new LinkedHashMap<>();
    context.put("action", request == null ? null : request.getAction());
    context.put("signerName", request == null ? null : request.getSignerName());
    context.put("remark", request == null ? null : request.getRemark());
    auditLogService.recordStructured(
        orgId, orgId, operatorId, AuthContext.getUsername(),
        "FIN_DISCHARGE_SETTLEMENT_CONFIRM", "FINANCE_FEE", id, "退住结算确认",
        beforeSnapshot, result, context);
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
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String moduleKey) {
    Long orgId = AuthContext.getOrgId();
    return Result.ok(feeManagementService.consumptionPage(orgId, pageNo, pageSize, elderId, from, to, category, keyword, moduleKey));
  }

  @PostMapping("/consumption")
  public Result<ConsumptionRecord> createConsumption(@Valid @RequestBody ConsumptionRecordCreateRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long operatorId = AuthContext.getStaffId();
    ConsumptionRecord result = feeManagementService.createConsumption(orgId, operatorId, request);
    auditLogService.recordStructured(
        orgId, orgId, operatorId, AuthContext.getUsername(),
        "FIN_CONSUMPTION_CREATE", "FINANCE_FEE", result.getId(), "消费登记",
        null, result, request);
    return Result.ok(result);
  }

  @GetMapping("/monthly-allocation/page")
  public Result<IPage<MonthlyAllocation>> monthlyAllocationPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String month,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Long elderId) {
    Long orgId = AuthContext.getOrgId();
    return Result.ok(feeManagementService.monthlyAllocationPage(orgId, pageNo, pageSize, month, status, elderId));
  }

  @PostMapping("/monthly-allocation")
  public Result<MonthlyAllocation> createMonthlyAllocation(@Valid @RequestBody MonthlyAllocationCreateRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long operatorId = AuthContext.getStaffId();
    MonthlyAllocation result = feeManagementService.createMonthlyAllocation(orgId, operatorId, request);
    auditLogService.recordStructured(
        orgId, orgId, operatorId, AuthContext.getUsername(),
        "FIN_MONTHLY_ALLOCATION_CREATE", "FINANCE_FEE", result.getId(), "月分摊费创建",
        null, result, request);
    return Result.ok(result);
  }

  @PostMapping("/monthly-allocation/preview")
  public Result<MonthlyAllocationPreviewResponse> previewMonthlyAllocation(
      @Valid @RequestBody MonthlyAllocationPreviewRequest request) {
    Long orgId = AuthContext.getOrgId();
    return Result.ok(feeManagementService.previewMonthlyAllocation(orgId, request));
  }

  @PostMapping("/monthly-allocation/{id}/rollback")
  @PreAuthorize("hasAnyRole('FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  public Result<MonthlyAllocation> rollbackMonthlyAllocation(
      @PathVariable Long id,
      @RequestBody(required = false) MonthlyAllocationRollbackRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long operatorId = AuthContext.getStaffId();
    MonthlyAllocation result = feeManagementService.rollbackMonthlyAllocation(orgId, operatorId, id, request);
    auditLogService.recordStructured(
        orgId, orgId, operatorId, AuthContext.getUsername(),
        "FIN_MONTHLY_ALLOCATION_ROLLBACK", "FINANCE_FEE", result.getId(), "月分摊回滚",
        null, result, request);
    return Result.ok(result);
  }
}
