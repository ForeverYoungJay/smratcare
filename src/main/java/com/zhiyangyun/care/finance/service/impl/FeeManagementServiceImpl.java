package com.zhiyangyun.care.finance.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderDischargeApply;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderDischargeApplyMapper;
import com.zhiyangyun.care.elder.model.lifecycle.ResidenceLifecycleConstants;
import com.zhiyangyun.care.finance.entity.AdmissionFeeAudit;
import com.zhiyangyun.care.finance.entity.ConsumptionRecord;
import com.zhiyangyun.care.finance.entity.DischargeFeeAudit;
import com.zhiyangyun.care.finance.entity.DischargeSettlement;
import com.zhiyangyun.care.finance.entity.ElderAccount;
import com.zhiyangyun.care.finance.entity.MonthlyAllocation;
import com.zhiyangyun.care.finance.mapper.AdmissionFeeAuditMapper;
import com.zhiyangyun.care.finance.mapper.ConsumptionRecordMapper;
import com.zhiyangyun.care.finance.mapper.DischargeFeeAuditMapper;
import com.zhiyangyun.care.finance.mapper.DischargeSettlementMapper;
import com.zhiyangyun.care.finance.mapper.ElderAccountMapper;
import com.zhiyangyun.care.finance.mapper.MonthlyAllocationMapper;
import com.zhiyangyun.care.finance.model.AdmissionFeeAuditCreateRequest;
import com.zhiyangyun.care.finance.model.ConsumptionRecordCreateRequest;
import com.zhiyangyun.care.finance.model.DischargeFeeAuditCreateRequest;
import com.zhiyangyun.care.finance.model.DischargeSettlementConfirmRequest;
import com.zhiyangyun.care.finance.model.DischargeSettlementCreateRequest;
import com.zhiyangyun.care.finance.model.ElderAccountAdjustRequest;
import com.zhiyangyun.care.finance.model.FeeAuditReviewRequest;
import com.zhiyangyun.care.finance.model.FeeWorkflowConstants;
import com.zhiyangyun.care.finance.model.MonthlyAllocationCreateRequest;
import com.zhiyangyun.care.finance.service.ElderAccountService;
import com.zhiyangyun.care.finance.service.FeeManagementService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeeManagementServiceImpl implements FeeManagementService {
  private final AdmissionFeeAuditMapper admissionFeeAuditMapper;
  private final DischargeFeeAuditMapper dischargeFeeAuditMapper;
  private final DischargeSettlementMapper dischargeSettlementMapper;
  private final ConsumptionRecordMapper consumptionRecordMapper;
  private final MonthlyAllocationMapper monthlyAllocationMapper;
  private final ElderMapper elderMapper;
  private final ElderAccountMapper elderAccountMapper;
  private final ElderDischargeApplyMapper elderDischargeApplyMapper;
  private final ElderAccountService elderAccountService;

  public FeeManagementServiceImpl(
      AdmissionFeeAuditMapper admissionFeeAuditMapper,
      DischargeFeeAuditMapper dischargeFeeAuditMapper,
      DischargeSettlementMapper dischargeSettlementMapper,
      ConsumptionRecordMapper consumptionRecordMapper,
      MonthlyAllocationMapper monthlyAllocationMapper,
      ElderMapper elderMapper,
      ElderAccountMapper elderAccountMapper,
      ElderDischargeApplyMapper elderDischargeApplyMapper,
      ElderAccountService elderAccountService) {
    this.admissionFeeAuditMapper = admissionFeeAuditMapper;
    this.dischargeFeeAuditMapper = dischargeFeeAuditMapper;
    this.dischargeSettlementMapper = dischargeSettlementMapper;
    this.consumptionRecordMapper = consumptionRecordMapper;
    this.monthlyAllocationMapper = monthlyAllocationMapper;
    this.elderMapper = elderMapper;
    this.elderAccountMapper = elderAccountMapper;
    this.elderDischargeApplyMapper = elderDischargeApplyMapper;
    this.elderAccountService = elderAccountService;
  }

  @Override
  public IPage<AdmissionFeeAudit> admissionAuditPage(Long orgId, long pageNo, long pageSize, Long elderId,
      String status) {
    var wrapper = Wrappers.lambdaQuery(AdmissionFeeAudit.class)
        .eq(AdmissionFeeAudit::getIsDeleted, 0)
        .eq(orgId != null, AdmissionFeeAudit::getOrgId, orgId)
        .eq(elderId != null, AdmissionFeeAudit::getElderId, elderId)
        .eq(hasText(status), AdmissionFeeAudit::getStatus, normalizeStatus(status))
        .orderByDesc(AdmissionFeeAudit::getCreateTime);
    return admissionFeeAuditMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  @Override
  @Transactional
  public AdmissionFeeAudit createAdmissionAudit(Long orgId, Long operatorId, AdmissionFeeAuditCreateRequest request) {
    validatePositiveAmount(request.getTotalAmount(), "totalAmount");
    validateNonNegativeAmount(request.getDepositAmount(), "depositAmount");
    ElderProfile elder = getElder(orgId, request.getElderId());

    AdmissionFeeAudit audit = new AdmissionFeeAudit();
    audit.setTenantId(orgId);
    audit.setOrgId(orgId);
    audit.setElderId(elder.getId());
    audit.setElderName(elder.getFullName());
    audit.setAdmissionId(request.getAdmissionId());
    audit.setTotalAmount(request.getTotalAmount());
    audit.setDepositAmount(request.getDepositAmount());
    audit.setStatus(FeeWorkflowConstants.AUDIT_PENDING);
    audit.setAuditRemark(request.getRemark());
    audit.setCreatedBy(operatorId);
    admissionFeeAuditMapper.insert(audit);
    return audit;
  }

  @Override
  @Transactional
  public AdmissionFeeAudit reviewAdmissionAudit(Long orgId, Long operatorId, Long id, FeeAuditReviewRequest request) {
    AdmissionFeeAudit audit = admissionFeeAuditMapper.selectById(id);
    if (audit == null || !Objects.equals(audit.getOrgId(), orgId) || Integer.valueOf(1).equals(audit.getIsDeleted())) {
      throw new IllegalArgumentException("入住费用审核记录不存在");
    }
    if (!FeeWorkflowConstants.AUDIT_PENDING.equals(audit.getStatus())) {
      throw new IllegalStateException("仅待审核状态可操作");
    }
    String status = normalizeReviewStatus(request.getStatus());
    if (FeeWorkflowConstants.AUDIT_REJECTED.equals(status)
        && (request.getReviewRemark() == null || request.getReviewRemark().isBlank())) {
      throw new IllegalArgumentException("驳回时请填写审核备注");
    }
    audit.setStatus(status);
    audit.setAuditRemark(request.getReviewRemark());
    audit.setReviewedBy(operatorId);
    audit.setReviewedTime(LocalDateTime.now());
    admissionFeeAuditMapper.updateById(audit);
    return audit;
  }

  @Override
  public IPage<DischargeFeeAudit> dischargeAuditPage(Long orgId, long pageNo, long pageSize, Long elderId,
      String status) {
    var wrapper = Wrappers.lambdaQuery(DischargeFeeAudit.class)
        .eq(DischargeFeeAudit::getIsDeleted, 0)
        .eq(orgId != null, DischargeFeeAudit::getOrgId, orgId)
        .eq(elderId != null, DischargeFeeAudit::getElderId, elderId)
        .eq(hasText(status), DischargeFeeAudit::getStatus, normalizeStatus(status))
        .orderByDesc(DischargeFeeAudit::getCreateTime);
    return dischargeFeeAuditMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  @Override
  @Transactional
  public DischargeFeeAudit createDischargeAudit(Long orgId, Long operatorId, DischargeFeeAuditCreateRequest request) {
    validateNonNegativeAmount(request.getPayableAmount(), "payableAmount");
    ElderProfile elder = getElder(orgId, request.getElderId());
    if (request.getDischargeApplyId() != null) {
      assertDischargeApplyApproved(orgId, request.getDischargeApplyId(), elder.getId());
    }

    DischargeFeeAudit audit = new DischargeFeeAudit();
    audit.setTenantId(orgId);
    audit.setOrgId(orgId);
    audit.setElderId(elder.getId());
    audit.setElderName(elder.getFullName());
    audit.setDischargeApplyId(request.getDischargeApplyId());
    audit.setPayableAmount(request.getPayableAmount());
    audit.setFeeItem(request.getFeeItem());
    audit.setDischargeFeeConfig(request.getDischargeFeeConfig());
    audit.setStatus(FeeWorkflowConstants.AUDIT_PENDING);
    audit.setAuditRemark(request.getRemark());
    audit.setCreatedBy(operatorId);
    dischargeFeeAuditMapper.insert(audit);
    return audit;
  }

  @Override
  @Transactional
  public DischargeFeeAudit reviewDischargeAudit(Long orgId, Long operatorId, Long id, FeeAuditReviewRequest request) {
    DischargeFeeAudit audit = dischargeFeeAuditMapper.selectById(id);
    if (audit == null || !Objects.equals(audit.getOrgId(), orgId) || Integer.valueOf(1).equals(audit.getIsDeleted())) {
      throw new IllegalArgumentException("退住费用审核记录不存在");
    }
    if (!FeeWorkflowConstants.AUDIT_PENDING.equals(audit.getStatus())) {
      throw new IllegalStateException("仅待审核状态可操作");
    }

    String status = normalizeReviewStatus(request.getStatus());
    if (FeeWorkflowConstants.AUDIT_REJECTED.equals(status)
        && (request.getReviewRemark() == null || request.getReviewRemark().isBlank())) {
      throw new IllegalArgumentException("驳回时请填写审核备注");
    }

    audit.setStatus(status);
    audit.setAuditRemark(request.getReviewRemark());
    audit.setReviewedBy(operatorId);
    audit.setReviewedTime(LocalDateTime.now());
    dischargeFeeAuditMapper.updateById(audit);

    if (FeeWorkflowConstants.AUDIT_APPROVED.equals(status)) {
      autoCreateSettlementFromApprovedAudit(orgId, operatorId, audit);
    }
    return audit;
  }

  @Override
  public IPage<DischargeSettlement> dischargeSettlementPage(Long orgId, long pageNo, long pageSize, Long elderId,
      String status) {
    var wrapper = Wrappers.lambdaQuery(DischargeSettlement.class)
        .eq(DischargeSettlement::getIsDeleted, 0)
        .eq(orgId != null, DischargeSettlement::getOrgId, orgId)
        .eq(elderId != null, DischargeSettlement::getElderId, elderId)
        .eq(hasText(status), DischargeSettlement::getStatus, normalizeStatus(status))
        .orderByDesc(DischargeSettlement::getCreateTime);
    return dischargeSettlementMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  @Override
  @Transactional
  public DischargeSettlement createDischargeSettlement(Long orgId, Long operatorId,
      DischargeSettlementCreateRequest request) {
    validateNonNegativeAmount(request.getPayableAmount(), "payableAmount");
    ElderProfile elder = getElder(orgId, request.getElderId());
    if (request.getDischargeApplyId() != null) {
      assertDischargeApplyApproved(orgId, request.getDischargeApplyId(), elder.getId());
      ensureSettlementNotExists(orgId, request.getDischargeApplyId());
    }

    elderAccountService.getOrCreate(orgId, elder.getId(), operatorId);
    BigDecimal balance = currentBalance(orgId, elder.getId());
    SettlementAmounts amounts = calculateSettlement(balance, request.getPayableAmount());

    DischargeSettlement settlement = new DischargeSettlement();
    settlement.setTenantId(orgId);
    settlement.setOrgId(orgId);
    settlement.setElderId(elder.getId());
    settlement.setElderName(elder.getFullName());
    settlement.setDischargeApplyId(request.getDischargeApplyId());
    settlement.setPayableAmount(request.getPayableAmount());
    settlement.setFeeItem(request.getFeeItem());
    settlement.setDischargeFeeConfig(request.getDischargeFeeConfig());
    settlement.setFromDepositAmount(amounts.fromDepositAmount());
    settlement.setRefundAmount(amounts.refundAmount());
    settlement.setSupplementAmount(amounts.supplementAmount());
    settlement.setStatus(FeeWorkflowConstants.SETTLEMENT_PENDING_CONFIRM);
    settlement.setRemark(request.getRemark());
    settlement.setCreatedBy(operatorId);
    dischargeSettlementMapper.insert(settlement);
    return settlement;
  }

  @Override
  @Transactional
  public DischargeSettlement confirmDischargeSettlement(Long orgId, Long operatorId, Long id,
      DischargeSettlementConfirmRequest request) {
    DischargeSettlement current = dischargeSettlementMapper.selectOne(
        Wrappers.lambdaQuery(DischargeSettlement.class)
            .eq(DischargeSettlement::getId, id)
            .eq(DischargeSettlement::getIsDeleted, 0)
            .eq(orgId != null, DischargeSettlement::getOrgId, orgId)
            .last("FOR UPDATE"));
    if (current == null) {
      throw new IllegalArgumentException("退住结算单不存在");
    }

    if (FeeWorkflowConstants.SETTLEMENT_SETTLED.equals(current.getStatus())) {
      return current;
    }
    if (!FeeWorkflowConstants.SETTLEMENT_PENDING_CONFIRM.equals(current.getStatus())) {
      throw new IllegalStateException("当前状态不允许确认结算");
    }

    int claimed = dischargeSettlementMapper.update(
        null,
        Wrappers.lambdaUpdate(DischargeSettlement.class)
            .eq(DischargeSettlement::getId, id)
            .eq(DischargeSettlement::getStatus, FeeWorkflowConstants.SETTLEMENT_PENDING_CONFIRM)
            .set(DischargeSettlement::getStatus, FeeWorkflowConstants.SETTLEMENT_PROCESSING));
    if (claimed != 1) {
      DischargeSettlement latest = dischargeSettlementMapper.selectById(id);
      if (latest != null && FeeWorkflowConstants.SETTLEMENT_SETTLED.equals(latest.getStatus())) {
        return latest;
      }
      throw new IllegalStateException("结算处理中，请稍后重试");
    }

    elderAccountService.getOrCreate(orgId, current.getElderId(), operatorId);
    BigDecimal balance = currentBalanceForUpdate(orgId, current.getElderId());
    SettlementAmounts amounts = calculateSettlement(balance, current.getPayableAmount());
    if (amounts.supplementAmount().compareTo(BigDecimal.ZERO) > 0) {
      throw new IllegalStateException("账户余额不足，请先完成预存充值后再结算");
    }

    if (amounts.fromDepositAmount().compareTo(BigDecimal.ZERO) > 0) {
      ElderAccountAdjustRequest debitFee = new ElderAccountAdjustRequest();
      debitFee.setElderId(current.getElderId());
      debitFee.setAmount(amounts.fromDepositAmount());
      debitFee.setDirection("DEBIT");
      debitFee.setRemark("退住结算扣费#" + current.getId());
      elderAccountService.adjust(orgId, operatorId, debitFee);
    }
    if (amounts.refundAmount().compareTo(BigDecimal.ZERO) > 0) {
      ElderAccountAdjustRequest debitRefund = new ElderAccountAdjustRequest();
      debitRefund.setElderId(current.getElderId());
      debitRefund.setAmount(amounts.refundAmount());
      debitRefund.setDirection("DEBIT");
      debitRefund.setRemark("退住结算退款#" + current.getId());
      elderAccountService.adjust(orgId, operatorId, debitRefund);
    }

    current.setFromDepositAmount(amounts.fromDepositAmount());
    current.setRefundAmount(amounts.refundAmount());
    current.setSupplementAmount(amounts.supplementAmount());
    current.setStatus(FeeWorkflowConstants.SETTLEMENT_SETTLED);
    current.setSettledBy(operatorId);
    current.setSettledTime(LocalDateTime.now());
    if (request != null && hasText(request.getRemark())) {
      current.setRemark(request.getRemark());
    }
    dischargeSettlementMapper.updateById(current);
    return current;
  }

  @Override
  public IPage<ConsumptionRecord> consumptionPage(Long orgId, long pageNo, long pageSize, Long elderId,
      String from, String to, String category) {
    var wrapper = Wrappers.lambdaQuery(ConsumptionRecord.class)
        .eq(ConsumptionRecord::getIsDeleted, 0)
        .eq(orgId != null, ConsumptionRecord::getOrgId, orgId)
        .eq(elderId != null, ConsumptionRecord::getElderId, elderId)
        .eq(hasText(category), ConsumptionRecord::getCategory, category)
        .orderByDesc(ConsumptionRecord::getConsumeDate)
        .orderByDesc(ConsumptionRecord::getCreateTime);
    if (hasText(from)) {
      wrapper.ge(ConsumptionRecord::getConsumeDate, LocalDate.parse(from));
    }
    if (hasText(to)) {
      wrapper.le(ConsumptionRecord::getConsumeDate, LocalDate.parse(to));
    }
    return consumptionRecordMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  @Override
  @Transactional
  public ConsumptionRecord createConsumption(Long orgId, Long operatorId, ConsumptionRecordCreateRequest request) {
    validatePositiveAmount(request.getAmount(), "amount");
    ElderProfile elder = getElder(orgId, request.getElderId());

    ConsumptionRecord record = new ConsumptionRecord();
    record.setTenantId(orgId);
    record.setOrgId(orgId);
    record.setElderId(elder.getId());
    record.setElderName(elder.getFullName());
    record.setConsumeDate(request.getConsumeDate());
    record.setAmount(request.getAmount());
    record.setCategory(request.getCategory());
    record.setSourceType(request.getSourceType());
    record.setSourceId(request.getSourceId());
    record.setRemark(request.getRemark());
    record.setCreatedBy(operatorId);
    consumptionRecordMapper.insert(record);
    return record;
  }

  @Override
  public IPage<MonthlyAllocation> monthlyAllocationPage(Long orgId, long pageNo, long pageSize,
      String month, String status) {
    var wrapper = Wrappers.lambdaQuery(MonthlyAllocation.class)
        .eq(MonthlyAllocation::getIsDeleted, 0)
        .eq(orgId != null, MonthlyAllocation::getOrgId, orgId)
        .eq(hasText(month), MonthlyAllocation::getAllocationMonth, month)
        .eq(hasText(status), MonthlyAllocation::getStatus, normalizeStatus(status))
        .orderByDesc(MonthlyAllocation::getCreateTime);
    return monthlyAllocationMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  @Override
  @Transactional
  public MonthlyAllocation createMonthlyAllocation(Long orgId, Long operatorId, MonthlyAllocationCreateRequest request) {
    validateNonNegativeAmount(request.getTotalAmount(), "totalAmount");
    if (request.getTargetCount() == null || request.getTargetCount() < 0) {
      throw new IllegalArgumentException("targetCount must be >= 0");
    }

    MonthlyAllocation allocation = new MonthlyAllocation();
    allocation.setTenantId(orgId);
    allocation.setOrgId(orgId);
    allocation.setAllocationMonth(request.getAllocationMonth());
    allocation.setAllocationName(request.getAllocationName());
    allocation.setTotalAmount(request.getTotalAmount());
    allocation.setTargetCount(request.getTargetCount());
    allocation.setStatus("DRAFT");
    allocation.setRemark(request.getRemark());
    allocation.setCreatedBy(operatorId);
    monthlyAllocationMapper.insert(allocation);
    return allocation;
  }

  private void autoCreateSettlementFromApprovedAudit(Long orgId, Long operatorId, DischargeFeeAudit audit) {
    if (audit.getDischargeApplyId() != null) {
      ensureSettlementNotExists(orgId, audit.getDischargeApplyId());
    }
    DischargeSettlementCreateRequest createRequest = new DischargeSettlementCreateRequest();
    createRequest.setElderId(audit.getElderId());
    createRequest.setDischargeApplyId(audit.getDischargeApplyId());
    createRequest.setPayableAmount(audit.getPayableAmount());
    createRequest.setFeeItem(audit.getFeeItem());
    createRequest.setDischargeFeeConfig(audit.getDischargeFeeConfig());
    createRequest.setRemark("退住费用审核通过自动生成");
    createDischargeSettlement(orgId, operatorId, createRequest);
  }

  private void ensureSettlementNotExists(Long orgId, Long dischargeApplyId) {
    long exists = dischargeSettlementMapper.selectCount(Wrappers.lambdaQuery(DischargeSettlement.class)
        .eq(DischargeSettlement::getIsDeleted, 0)
        .eq(orgId != null, DischargeSettlement::getOrgId, orgId)
        .eq(DischargeSettlement::getDischargeApplyId, dischargeApplyId));
    if (exists > 0) {
      throw new IllegalStateException("该退住申请已存在结算单");
    }
  }

  private void assertDischargeApplyApproved(Long orgId, Long dischargeApplyId, Long elderId) {
    ElderDischargeApply apply = elderDischargeApplyMapper.selectOne(Wrappers.lambdaQuery(ElderDischargeApply.class)
        .eq(ElderDischargeApply::getIsDeleted, 0)
        .eq(orgId != null, ElderDischargeApply::getOrgId, orgId)
        .eq(ElderDischargeApply::getId, dischargeApplyId)
        .last("LIMIT 1"));
    if (apply == null || !Objects.equals(apply.getElderId(), elderId)) {
      throw new IllegalArgumentException("退住申请不存在或不匹配");
    }
    if (!ResidenceLifecycleConstants.DISCHARGE_APPLY_APPROVED.equalsIgnoreCase(apply.getStatus())) {
      throw new IllegalStateException("退住申请未审核通过，不能进入费用审核/结算");
    }
  }

  private ElderProfile getElder(Long orgId, Long elderId) {
    ElderProfile elder = elderMapper.selectOne(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(orgId != null, ElderProfile::getOrgId, orgId)
        .eq(ElderProfile::getId, elderId)
        .last("LIMIT 1"));
    if (elder == null) {
      throw new IllegalArgumentException("老人不存在");
    }
    return elder;
  }

  private BigDecimal currentBalance(Long orgId, Long elderId) {
    ElderAccount account = elderAccountMapper.selectOne(Wrappers.lambdaQuery(ElderAccount.class)
        .eq(ElderAccount::getIsDeleted, 0)
        .eq(orgId != null, ElderAccount::getOrgId, orgId)
        .eq(ElderAccount::getElderId, elderId)
        .last("LIMIT 1"));
    if (account == null || account.getBalance() == null) {
      return BigDecimal.ZERO;
    }
    return account.getBalance();
  }

  private BigDecimal currentBalanceForUpdate(Long orgId, Long elderId) {
    ElderAccount account = elderAccountMapper.selectOne(Wrappers.lambdaQuery(ElderAccount.class)
        .eq(ElderAccount::getIsDeleted, 0)
        .eq(orgId != null, ElderAccount::getOrgId, orgId)
        .eq(ElderAccount::getElderId, elderId)
        .last("FOR UPDATE"));
    if (account == null || account.getBalance() == null) {
      return BigDecimal.ZERO;
    }
    return account.getBalance();
  }

  private SettlementAmounts calculateSettlement(BigDecimal balance, BigDecimal payable) {
    BigDecimal safeBalance = balance == null ? BigDecimal.ZERO : balance;
    BigDecimal safePayable = payable == null ? BigDecimal.ZERO : payable;
    BigDecimal fromDeposit = safeBalance.min(safePayable);
    BigDecimal supplement = safePayable.subtract(fromDeposit);
    BigDecimal refund = safeBalance.subtract(fromDeposit);
    if (supplement.compareTo(BigDecimal.ZERO) < 0) {
      supplement = BigDecimal.ZERO;
    }
    if (refund.compareTo(BigDecimal.ZERO) < 0) {
      refund = BigDecimal.ZERO;
    }
    return new SettlementAmounts(fromDeposit, refund, supplement);
  }

  private String normalizeReviewStatus(String status) {
    String normalized = normalizeStatus(status);
    if (!FeeWorkflowConstants.AUDIT_APPROVED.equals(normalized)
        && !FeeWorkflowConstants.AUDIT_REJECTED.equals(normalized)) {
      throw new IllegalArgumentException("状态仅支持 APPROVED 或 REJECTED");
    }
    return normalized;
  }

  private String normalizeStatus(String status) {
    return status == null ? null : status.trim().toUpperCase(Locale.ROOT);
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }

  private void validatePositiveAmount(BigDecimal amount, String fieldName) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException(fieldName + " must be positive");
    }
  }

  private void validateNonNegativeAmount(BigDecimal amount, String fieldName) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException(fieldName + " must be >= 0");
    }
  }

  private record SettlementAmounts(BigDecimal fromDepositAmount, BigDecimal refundAmount,
                                   BigDecimal supplementAmount) {}
}
