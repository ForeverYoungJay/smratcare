package com.zhiyangyun.care.finance.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.bill.entity.BillItem;
import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillItemMapper;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderDischargeApply;
import com.zhiyangyun.care.elder.mapper.BedMapper;
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
import com.zhiyangyun.care.finance.model.MonthlyAllocationPreviewRequest;
import com.zhiyangyun.care.finance.model.MonthlyAllocationPreviewResponse;
import com.zhiyangyun.care.finance.model.MonthlyAllocationRollbackRequest;
import com.zhiyangyun.care.finance.model.ReconcileResponse;
import com.zhiyangyun.care.finance.service.ElderAccountService;
import com.zhiyangyun.care.finance.service.FeeManagementService;
import com.zhiyangyun.care.finance.service.FinanceService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeeManagementServiceImpl implements FeeManagementService {
  private final AdmissionFeeAuditMapper admissionFeeAuditMapper;
  private final DischargeFeeAuditMapper dischargeFeeAuditMapper;
  private final DischargeSettlementMapper dischargeSettlementMapper;
  private final ConsumptionRecordMapper consumptionRecordMapper;
  private final MonthlyAllocationMapper monthlyAllocationMapper;
  private final BillMonthlyMapper billMonthlyMapper;
  private final BillItemMapper billItemMapper;
  private final BedMapper bedMapper;
  private final ElderMapper elderMapper;
  private final ElderAccountMapper elderAccountMapper;
  private final ElderDischargeApplyMapper elderDischargeApplyMapper;
  private final ElderAccountService elderAccountService;
  private final FinanceService financeService;

  public FeeManagementServiceImpl(
      AdmissionFeeAuditMapper admissionFeeAuditMapper,
      DischargeFeeAuditMapper dischargeFeeAuditMapper,
      DischargeSettlementMapper dischargeSettlementMapper,
      ConsumptionRecordMapper consumptionRecordMapper,
      MonthlyAllocationMapper monthlyAllocationMapper,
      BillMonthlyMapper billMonthlyMapper,
      BillItemMapper billItemMapper,
      BedMapper bedMapper,
      ElderMapper elderMapper,
      ElderAccountMapper elderAccountMapper,
      ElderDischargeApplyMapper elderDischargeApplyMapper,
      ElderAccountService elderAccountService,
      FinanceService financeService) {
    this.admissionFeeAuditMapper = admissionFeeAuditMapper;
    this.dischargeFeeAuditMapper = dischargeFeeAuditMapper;
    this.dischargeSettlementMapper = dischargeSettlementMapper;
    this.consumptionRecordMapper = consumptionRecordMapper;
    this.monthlyAllocationMapper = monthlyAllocationMapper;
    this.billMonthlyMapper = billMonthlyMapper;
    this.billItemMapper = billItemMapper;
    this.bedMapper = bedMapper;
    this.elderMapper = elderMapper;
    this.elderAccountMapper = elderAccountMapper;
    this.elderDischargeApplyMapper = elderDischargeApplyMapper;
    this.elderAccountService = elderAccountService;
    this.financeService = financeService;
  }

  @Override
  public IPage<AdmissionFeeAudit> admissionAuditPage(Long orgId, long pageNo, long pageSize, Long elderId,
      String status, String keyword) {
    String normalizedKeyword = normalizeOptionalText(keyword);
    var wrapper = Wrappers.lambdaQuery(AdmissionFeeAudit.class)
        .eq(AdmissionFeeAudit::getIsDeleted, 0)
        .eq(orgId != null, AdmissionFeeAudit::getOrgId, orgId)
        .eq(elderId != null, AdmissionFeeAudit::getElderId, elderId)
        .eq(hasText(status), AdmissionFeeAudit::getStatus, normalizeStatus(status))
        .and(hasText(normalizedKeyword),
            q -> q.like(AdmissionFeeAudit::getElderName, normalizedKeyword)
                .or()
                .like(AdmissionFeeAudit::getAuditRemark, normalizedKeyword))
        .orderByDesc(AdmissionFeeAudit::getCreateTime);
    return admissionFeeAuditMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  @Override
  @Transactional
  public AdmissionFeeAudit createAdmissionAudit(Long orgId, Long operatorId, AdmissionFeeAuditCreateRequest request) {
    validatePositiveAmount(request.getTotalAmount(), "totalAmount");
    validateNonNegativeAmount(request.getDepositAmount(), "depositAmount");
    ElderProfile elder = getElder(orgId, request.getElderId());
    ensureNoPendingAdmissionAudit(orgId, elder.getId(), request.getAdmissionId());

    AdmissionFeeAudit audit = new AdmissionFeeAudit();
    audit.setTenantId(orgId);
    audit.setOrgId(orgId);
    audit.setElderId(elder.getId());
    audit.setElderName(elder.getFullName());
    audit.setAdmissionId(request.getAdmissionId());
    audit.setTotalAmount(request.getTotalAmount());
    audit.setDepositAmount(request.getDepositAmount());
    audit.setStatus(FeeWorkflowConstants.AUDIT_PENDING);
    audit.setAuditRemark(normalizeOptionalText(request.getRemark()));
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
    audit.setAuditRemark(normalizeOptionalText(request.getReviewRemark()));
    audit.setReviewedBy(operatorId);
    audit.setReviewedTime(LocalDateTime.now());
    admissionFeeAuditMapper.updateById(audit);
    return audit;
  }

  @Override
  public IPage<DischargeFeeAudit> dischargeAuditPage(Long orgId, long pageNo, long pageSize, Long elderId,
      String status, String keyword) {
    String normalizedKeyword = normalizeOptionalText(keyword);
    var wrapper = Wrappers.lambdaQuery(DischargeFeeAudit.class)
        .eq(DischargeFeeAudit::getIsDeleted, 0)
        .eq(orgId != null, DischargeFeeAudit::getOrgId, orgId)
        .eq(elderId != null, DischargeFeeAudit::getElderId, elderId)
        .eq(hasText(status), DischargeFeeAudit::getStatus, normalizeStatus(status))
        .and(hasText(normalizedKeyword),
            q -> q.like(DischargeFeeAudit::getElderName, normalizedKeyword)
                .or()
                .like(DischargeFeeAudit::getFeeItem, normalizedKeyword)
                .or()
                .like(DischargeFeeAudit::getDischargeFeeConfig, normalizedKeyword)
                .or()
                .like(DischargeFeeAudit::getAuditRemark, normalizedKeyword))
        .orderByDesc(DischargeFeeAudit::getCreateTime);
    return dischargeFeeAuditMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  @Override
  @Transactional
  public DischargeFeeAudit createDischargeAudit(Long orgId, Long operatorId, DischargeFeeAuditCreateRequest request) {
    validateNonNegativeAmount(request.getPayableAmount(), "payableAmount");
    ElderProfile elder = getElder(orgId, request.getElderId());
    ensureNoPendingDischargeAudit(orgId, elder.getId(), request.getDischargeApplyId());
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
    audit.setFeeItem(normalizeOptionalText(request.getFeeItem()));
    audit.setDischargeFeeConfig(normalizeOptionalText(request.getDischargeFeeConfig()));
    audit.setStatus(FeeWorkflowConstants.AUDIT_PENDING);
    audit.setAuditRemark(normalizeOptionalText(request.getRemark()));
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
    audit.setAuditRemark(normalizeOptionalText(request.getReviewRemark()));
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
      String status, String keyword) {
    String normalizedKeyword = normalizeOptionalText(keyword);
    var wrapper = Wrappers.lambdaQuery(DischargeSettlement.class)
        .eq(DischargeSettlement::getIsDeleted, 0)
        .eq(orgId != null, DischargeSettlement::getOrgId, orgId)
        .eq(elderId != null, DischargeSettlement::getElderId, elderId)
        .eq(hasText(status), DischargeSettlement::getStatus, normalizeStatus(status))
        .and(hasText(normalizedKeyword),
            q -> q.like(DischargeSettlement::getElderName, normalizedKeyword)
                .or()
                .like(DischargeSettlement::getFeeItem, normalizedKeyword)
                .or()
                .like(DischargeSettlement::getDischargeFeeConfig, normalizedKeyword)
                .or()
                .like(DischargeSettlement::getRemark, normalizedKeyword))
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
    settlement.setDetailNo(generateDetailNo(elder.getId()));
    settlement.setElderId(elder.getId());
    settlement.setElderName(elder.getFullName());
    settlement.setDischargeApplyId(request.getDischargeApplyId());
    settlement.setPayableAmount(request.getPayableAmount());
    settlement.setFeeItem(normalizeOptionalText(request.getFeeItem()));
    settlement.setDischargeFeeConfig(normalizeOptionalText(request.getDischargeFeeConfig()));
    settlement.setFromDepositAmount(amounts.fromDepositAmount());
    settlement.setRefundAmount(amounts.refundAmount());
    settlement.setSupplementAmount(amounts.supplementAmount());
    settlement.setStatus(FeeWorkflowConstants.SETTLEMENT_PENDING_CONFIRM);
    settlement.setFrontdeskApproved(0);
    settlement.setNursingApproved(0);
    settlement.setFinanceRefunded(0);
    settlement.setRemark(normalizeOptionalText(request.getRemark()));
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

    String action = request == null || !hasText(request.getAction())
        ? "FINANCE_REFUND"
        : request.getAction().trim().toUpperCase(Locale.ROOT);
    String signerName = request == null ? null : normalizeOptionalText(request.getSignerName());
    String remark = request == null ? null : normalizeOptionalText(request.getRemark());

    if ("FRONTDESK_APPROVE".equals(action)) {
      current.setFrontdeskApproved(1);
      current.setFrontdeskSignerName(hasText(signerName) ? signerName : "前台");
      current.setFrontdeskSignedTime(LocalDateTime.now());
      current.setStatus(FeeWorkflowConstants.SETTLEMENT_PROCESSING);
      if (hasText(remark)) {
        current.setRemark(mergeRemark(current.getRemark(), remark));
      }
      dischargeSettlementMapper.updateById(current);
      return current;
    }

    if ("NURSING_APPROVE".equals(action)) {
      current.setNursingApproved(1);
      current.setNursingSignerName(hasText(signerName) ? signerName : "护理部");
      current.setNursingSignedTime(LocalDateTime.now());
      current.setStatus(FeeWorkflowConstants.SETTLEMENT_PROCESSING);
      if (hasText(remark)) {
        current.setRemark(mergeRemark(current.getRemark(), remark));
      }
      dischargeSettlementMapper.updateById(current);
      return current;
    }

    if (!"FINANCE_REFUND".equals(action)) {
      throw new IllegalArgumentException("action 仅支持 FRONTDESK_APPROVE/NURSING_APPROVE/FINANCE_REFUND");
    }

    if (!Integer.valueOf(1).equals(current.getFrontdeskApproved())
        || !Integer.valueOf(1).equals(current.getNursingApproved())) {
      throw new IllegalStateException("需前台与护理部审核签字后，方可财务退款");
    }

    int claimed = dischargeSettlementMapper.update(
        null,
        Wrappers.lambdaUpdate(DischargeSettlement.class)
            .eq(DischargeSettlement::getId, id)
            .in(DischargeSettlement::getStatus, FeeWorkflowConstants.SETTLEMENT_PENDING_CONFIRM,
                FeeWorkflowConstants.SETTLEMENT_PROCESSING)
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

    List<String> linkageDetails = new ArrayList<>();
    linkageDetails.add("费用清单:" + buildFeeSnapshot(current, amounts));
    linkageDetails.add("押金处理:" + buildDepositSummary(amounts));
    linkageDetails.add("床位释放:" + releaseBedAndSyncElder(orgId, current.getElderId()));
    linkageDetails.add("自动对账:" + runDailyReconcile(orgId));

    current.setFromDepositAmount(amounts.fromDepositAmount());
    current.setRefundAmount(amounts.refundAmount());
    current.setSupplementAmount(amounts.supplementAmount());
    current.setStatus(FeeWorkflowConstants.SETTLEMENT_SETTLED);
    current.setFinanceRefunded(1);
    current.setFinanceRefundOperatorName(hasText(signerName) ? signerName : "财务");
    current.setFinanceRefundTime(LocalDateTime.now());
    current.setSettledBy(operatorId);
    current.setSettledTime(LocalDateTime.now());
    String linkageRemark = String.join("；", linkageDetails);
    String mergedRemark = mergeRemark(current.getRemark(), linkageRemark);
    if (hasText(remark)) {
      mergedRemark = mergeRemark(mergedRemark, remark);
    }
    current.setRemark(mergedRemark);
    dischargeSettlementMapper.updateById(current);
    return current;
  }

  @Override
  public IPage<ConsumptionRecord> consumptionPage(Long orgId, long pageNo, long pageSize, Long elderId,
      String from, String to, String category, String keyword) {
    LocalDate fromDate = parseDateOrNull(from, "from");
    LocalDate toDate = parseDateOrNull(to, "to");
    if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
      throw new IllegalArgumentException("开始日期不能晚于结束日期");
    }
    String normalizedCategory = normalizeConsumptionCategory(category);
    String normalizedKeyword = normalizeOptionalText(keyword);
    var wrapper = Wrappers.lambdaQuery(ConsumptionRecord.class)
        .eq(ConsumptionRecord::getIsDeleted, 0)
        .eq(orgId != null, ConsumptionRecord::getOrgId, orgId)
        .eq(elderId != null, ConsumptionRecord::getElderId, elderId)
        .eq(hasText(normalizedCategory), ConsumptionRecord::getCategory, normalizedCategory)
        .and(hasText(normalizedKeyword),
            q -> q.like(ConsumptionRecord::getElderName, normalizedKeyword)
                .or()
                .like(ConsumptionRecord::getCategory, normalizedKeyword)
                .or()
                .like(ConsumptionRecord::getSourceType, normalizedKeyword)
                .or()
                .like(ConsumptionRecord::getRemark, normalizedKeyword))
        .orderByDesc(ConsumptionRecord::getConsumeDate)
        .orderByDesc(ConsumptionRecord::getCreateTime);
    if (fromDate != null) {
      wrapper.ge(ConsumptionRecord::getConsumeDate, fromDate);
    }
    if (toDate != null) {
      wrapper.le(ConsumptionRecord::getConsumeDate, toDate);
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
    record.setCategory(normalizeConsumptionCategory(request.getCategory()));
    record.setSourceType(normalizeOptionalText(request.getSourceType()));
    record.setSourceId(request.getSourceId());
    record.setRemark(normalizeOptionalText(request.getRemark()));
    record.setCreatedBy(operatorId);
    consumptionRecordMapper.insert(record);
    return record;
  }

  @Override
  public IPage<MonthlyAllocation> monthlyAllocationPage(Long orgId, long pageNo, long pageSize,
      String month, String status, Long elderId) {
    var wrapper = Wrappers.lambdaQuery(MonthlyAllocation.class)
        .eq(MonthlyAllocation::getIsDeleted, 0)
        .eq(orgId != null, MonthlyAllocation::getOrgId, orgId)
        .eq(hasText(month), MonthlyAllocation::getAllocationMonth, month)
        .eq(hasText(status), MonthlyAllocation::getStatus, normalizeStatus(status))
        .apply(elderId != null, "FIND_IN_SET({0}, elder_ids)", elderId)
        .orderByDesc(MonthlyAllocation::getCreateTime);
    return monthlyAllocationMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  @Override
  public MonthlyAllocationPreviewResponse previewMonthlyAllocation(Long orgId, MonthlyAllocationPreviewRequest request) {
    validateNonNegativeAmount(request.getTotalAmount(), "totalAmount");
    List<ElderProfile> elders = listAllocationElders(orgId, request.getElderIds());
    if (elders.isEmpty()) {
      throw new IllegalArgumentException("请选择分摊老人");
    }
    return buildPreviewResponse(request.getAllocationMonth(), request.getAllocationName(), request.getTotalAmount(),
        request.getRemark(), elders);
  }

  @Override
  @Transactional
  public MonthlyAllocation createMonthlyAllocation(Long orgId, Long operatorId, MonthlyAllocationCreateRequest request) {
    validateNonNegativeAmount(request.getTotalAmount(), "totalAmount");
    List<Long> elderIds = normalizeElderIds(request.getElderIds());
    List<ElderProfile> elders = listAllocationElders(orgId, elderIds);
    if (elders.isEmpty()) {
      throw new IllegalArgumentException("请选择分摊老人");
    }
    int targetCount = elderIds.size();
    if (request.getTotalAmount().compareTo(BigDecimal.ZERO) > 0 && targetCount == 0) {
      throw new IllegalArgumentException("totalAmount > 0 时目标人数不能为 0");
    }

    MonthlyAllocation allocation = new MonthlyAllocation();
    allocation.setTenantId(orgId);
    allocation.setOrgId(orgId);
    allocation.setAllocationMonth(request.getAllocationMonth());
    allocation.setAllocationName(normalizeOptionalText(request.getAllocationName()));
    allocation.setTotalAmount(request.getTotalAmount());
    allocation.setTargetCount(targetCount);
    allocation.setElderIds(elderIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
    allocation.setElderSnapshotJson(buildElderSnapshotJson(elders));
    allocation.setAvgAmount(
        targetCount <= 0
            ? BigDecimal.ZERO
            : request.getTotalAmount().divide(BigDecimal.valueOf(targetCount), 2, RoundingMode.HALF_UP));
    allocation.setStatus("CONFIRMED");
    allocation.setRemark(normalizeOptionalText(request.getRemark()));
    allocation.setReviewedBy(operatorId);
    allocation.setReviewedTime(LocalDateTime.now());
    allocation.setCreatedBy(operatorId);
    monthlyAllocationMapper.insert(allocation);
    writeAllocationConsumptionRecords(orgId, operatorId, allocation, elders);
    return allocation;
  }

  @Override
  @Transactional
  public MonthlyAllocation rollbackMonthlyAllocation(
      Long orgId,
      Long operatorId,
      Long id,
      MonthlyAllocationRollbackRequest request) {
    MonthlyAllocation allocation = monthlyAllocationMapper.selectOne(
        Wrappers.lambdaQuery(MonthlyAllocation.class)
            .eq(MonthlyAllocation::getIsDeleted, 0)
            .eq(MonthlyAllocation::getId, id)
            .eq(orgId != null, MonthlyAllocation::getOrgId, orgId)
            .last("FOR UPDATE"));
    if (allocation == null) {
      throw new IllegalArgumentException("分摊记录不存在");
    }
    if ("CANCELLED".equalsIgnoreCase(allocation.getStatus())) {
      return allocation;
    }
    consumptionRecordMapper.update(
        null,
        Wrappers.lambdaUpdate(ConsumptionRecord.class)
            .set(ConsumptionRecord::getIsDeleted, 1)
            .eq(ConsumptionRecord::getIsDeleted, 0)
            .eq(orgId != null, ConsumptionRecord::getOrgId, orgId)
            .eq(ConsumptionRecord::getSourceType, "MONTHLY_ALLOCATION")
            .eq(ConsumptionRecord::getSourceId, allocation.getId()));
    List<BillItem> allocationItems = billItemMapper.selectList(
        Wrappers.lambdaQuery(BillItem.class)
            .eq(BillItem::getIsDeleted, 0)
            .eq(orgId != null, BillItem::getOrgId, orgId)
            .eq(BillItem::getItemType, "ALLOCATION")
            .eq(BillItem::getRefOrderId, allocation.getId()));
    if (!allocationItems.isEmpty()) {
      for (BillItem item : allocationItems) {
        item.setIsDeleted(1);
        billItemMapper.updateById(item);
      }
      adjustBillsByAllocationItems(orgId, allocationItems, true);
    }
    allocation.setStatus("CANCELLED");
    allocation.setRollbackBy(operatorId);
    allocation.setRollbackReason(normalizeOptionalText(request == null ? null : request.getReason()));
    allocation.setRollbackTime(LocalDateTime.now());
    monthlyAllocationMapper.updateById(allocation);
    return allocation;
  }

  private void writeAllocationConsumptionRecords(
      Long orgId,
      Long operatorId,
      MonthlyAllocation allocation,
      List<ElderProfile> elders) {
    if (allocation == null || elders == null || elders.isEmpty()) {
      return;
    }
    BigDecimal total = allocation.getTotalAmount() == null ? BigDecimal.ZERO : allocation.getTotalAmount();
    BigDecimal average = total.divide(BigDecimal.valueOf(elders.size()), 2, RoundingMode.DOWN);
    BigDecimal allocated = average.multiply(BigDecimal.valueOf(elders.size()));
    BigDecimal remainder = total.subtract(allocated);
    LocalDate consumeDate;
    try {
      consumeDate = YearMonth.parse(allocation.getAllocationMonth()).atDay(1);
    } catch (Exception ex) {
      consumeDate = LocalDate.now();
    }
    for (int index = 0; index < elders.size(); index += 1) {
      ElderProfile elder = elders.get(index);
      BigDecimal amount = index == elders.size() - 1 ? average.add(remainder) : average;
      ConsumptionRecord record = new ConsumptionRecord();
      record.setTenantId(orgId);
      record.setOrgId(orgId);
      record.setElderId(elder.getId());
      record.setElderName(elder.getFullName());
      record.setConsumeDate(consumeDate);
      record.setAmount(amount);
      record.setCategory("ALLOCATION");
      record.setSourceType("MONTHLY_ALLOCATION");
      record.setSourceId(allocation.getId());
      record.setRemark(buildAllocationRemark(allocation, amount));
      record.setCreatedBy(operatorId);
      consumptionRecordMapper.insert(record);
      upsertAllocationBillItem(orgId, allocation, elder, amount);
    }
  }

  private void upsertAllocationBillItem(
      Long orgId,
      MonthlyAllocation allocation,
      ElderProfile elder,
      BigDecimal amount) {
    if (allocation == null || elder == null || elder.getId() == null) {
      return;
    }
    String month = allocation.getAllocationMonth();
    if (!hasText(month)) {
      month = YearMonth.now().toString();
    }
    BillMonthly monthly = billMonthlyMapper.selectOne(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getIsDeleted, 0)
            .eq(orgId != null, BillMonthly::getOrgId, orgId)
            .eq(BillMonthly::getElderId, elder.getId())
            .eq(BillMonthly::getBillMonth, month)
            .last("LIMIT 1"));
    if (monthly == null) {
      monthly = new BillMonthly();
      monthly.setOrgId(orgId);
      monthly.setElderId(elder.getId());
      monthly.setBillMonth(month);
      monthly.setTotalAmount(BigDecimal.ZERO);
      monthly.setPaidAmount(BigDecimal.ZERO);
      monthly.setOutstandingAmount(BigDecimal.ZERO);
      monthly.setStatus(0);
      billMonthlyMapper.insert(monthly);
    }
    BillItem item = new BillItem();
    item.setOrgId(orgId);
    item.setBillMonthlyId(monthly.getId());
    item.setItemType("ALLOCATION");
    item.setItemName(hasText(allocation.getAllocationName()) ? allocation.getAllocationName() : "月分摊费");
    item.setAmount(amount);
    item.setRefOrderId(allocation.getId());
    item.setRemark(buildAllocationRemark(allocation, amount));
    billItemMapper.insert(item);
    syncBillMonthlyAmounts(monthly, amount, false);
    billMonthlyMapper.updateById(monthly);
  }

  private void adjustBillsByAllocationItems(Long orgId, List<BillItem> items, boolean subtract) {
    if (items == null || items.isEmpty()) {
      return;
    }
    var billAmountMap = new java.util.HashMap<Long, BigDecimal>();
    for (BillItem item : items) {
      if (item.getBillMonthlyId() == null) continue;
      billAmountMap.merge(item.getBillMonthlyId(), safe(item.getAmount()), BigDecimal::add);
    }
    for (var entry : billAmountMap.entrySet()) {
      BillMonthly monthly = billMonthlyMapper.selectOne(
          Wrappers.lambdaQuery(BillMonthly.class)
              .eq(BillMonthly::getId, entry.getKey())
              .eq(BillMonthly::getIsDeleted, 0)
              .eq(orgId != null, BillMonthly::getOrgId, orgId)
              .last("LIMIT 1"));
      if (monthly == null) continue;
      syncBillMonthlyAmounts(monthly, entry.getValue(), subtract);
      billMonthlyMapper.updateById(monthly);
    }
  }

  private void syncBillMonthlyAmounts(BillMonthly monthly, BigDecimal amount, boolean subtract) {
    if (monthly == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      return;
    }
    BigDecimal total = safe(monthly.getTotalAmount());
    BigDecimal paid = safe(monthly.getPaidAmount());
    BigDecimal delta = subtract ? amount.negate() : amount;
    BigDecimal newTotal = total.add(delta);
    if (newTotal.compareTo(BigDecimal.ZERO) < 0) {
      newTotal = BigDecimal.ZERO;
    }
    monthly.setTotalAmount(newTotal);
    BigDecimal outstanding = newTotal.subtract(paid);
    if (outstanding.compareTo(BigDecimal.ZERO) < 0) {
      outstanding = BigDecimal.ZERO;
    }
    monthly.setOutstandingAmount(outstanding);
    if (Integer.valueOf(9).equals(monthly.getStatus())) {
      return;
    }
    if (newTotal.compareTo(BigDecimal.ZERO) <= 0) {
      monthly.setStatus(0);
      return;
    }
    if (paid.compareTo(BigDecimal.ZERO) <= 0) {
      monthly.setStatus(0);
      return;
    }
    monthly.setStatus(outstanding.compareTo(BigDecimal.ZERO) == 0 ? 2 : 1);
  }

  private String buildAllocationRemark(MonthlyAllocation allocation, BigDecimal amount) {
    String month = allocation.getAllocationMonth() == null ? "-" : allocation.getAllocationMonth();
    String name = allocation.getAllocationName() == null ? "分摊项目" : allocation.getAllocationName();
    String extra = allocation.getRemark() == null || allocation.getRemark().isBlank()
        ? ""
        : ("；备注:" + allocation.getRemark().trim());
    return "月分摊[" + name + "] 月份:" + month + " 分摊金额:" + amount + extra;
  }

  private MonthlyAllocationPreviewResponse buildPreviewResponse(
      String allocationMonth,
      String allocationName,
      BigDecimal totalAmount,
      String remark,
      List<ElderProfile> elders) {
    MonthlyAllocationPreviewResponse response = new MonthlyAllocationPreviewResponse();
    response.setAllocationMonth(normalizeOptionalText(allocationMonth));
    response.setAllocationName(normalizeOptionalText(allocationName));
    response.setTotalAmount(totalAmount);
    response.setTargetCount(elders.size());
    response.setRemark(normalizeOptionalText(remark));
    if (elders.isEmpty()) {
      response.setAvgAmount(BigDecimal.ZERO);
      return response;
    }
    BigDecimal average = totalAmount.divide(BigDecimal.valueOf(elders.size()), 2, RoundingMode.DOWN);
    BigDecimal allocated = average.multiply(BigDecimal.valueOf(elders.size()));
    BigDecimal remainder = totalAmount.subtract(allocated);
    response.setAvgAmount(totalAmount.divide(BigDecimal.valueOf(elders.size()), 2, RoundingMode.HALF_UP));
    for (int index = 0; index < elders.size(); index += 1) {
      ElderProfile elder = elders.get(index);
      BigDecimal amount = index == elders.size() - 1 ? average.add(remainder) : average;
      MonthlyAllocationPreviewResponse.Row row = new MonthlyAllocationPreviewResponse.Row();
      row.setElderId(elder.getId());
      row.setElderName(elder.getFullName());
      row.setAmount(amount);
      response.getRows().add(row);
    }
    return response;
  }

  private List<Long> normalizeElderIds(List<Long> source) {
    if (source == null || source.isEmpty()) {
      return List.of();
    }
    return source.stream().filter(Objects::nonNull).distinct().toList();
  }

  private List<ElderProfile> listAllocationElders(Long orgId, List<Long> elderIds) {
    List<Long> normalized = normalizeElderIds(elderIds);
    if (normalized.isEmpty()) {
      return List.of();
    }
    List<ElderProfile> elders = elderMapper.selectList(
        Wrappers.lambdaQuery(ElderProfile.class)
            .eq(ElderProfile::getIsDeleted, 0)
            .eq(orgId != null, ElderProfile::getOrgId, orgId)
            .in(ElderProfile::getId, normalized));
    if (elders.size() != normalized.size()) {
      throw new IllegalArgumentException("存在无效老人，无法分摊");
    }
    return elders;
  }

  private String buildElderSnapshotJson(List<ElderProfile> elders) {
    if (elders == null || elders.isEmpty()) {
      return "[]";
    }
    StringBuilder builder = new StringBuilder("[");
    for (int index = 0; index < elders.size(); index += 1) {
      ElderProfile elder = elders.get(index);
      if (index > 0) {
        builder.append(',');
      }
      builder.append("{\"elderId\":")
          .append(elder.getId() == null ? "null" : elder.getId())
          .append(",\"elderName\":\"")
          .append(escapeJson(elder.getFullName()))
          .append("\"}");
    }
    builder.append(']');
    return builder.toString();
  }

  private String escapeJson(String text) {
    String value = text == null ? "" : text;
    return value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r");
  }

  private BigDecimal safe(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
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

  private void ensureNoPendingAdmissionAudit(Long orgId, Long elderId, Long admissionId) {
    var query = Wrappers.lambdaQuery(AdmissionFeeAudit.class)
        .eq(AdmissionFeeAudit::getIsDeleted, 0)
        .eq(orgId != null, AdmissionFeeAudit::getOrgId, orgId)
        .eq(AdmissionFeeAudit::getElderId, elderId)
        .eq(AdmissionFeeAudit::getStatus, FeeWorkflowConstants.AUDIT_PENDING);
    if (admissionId != null) {
      query.eq(AdmissionFeeAudit::getAdmissionId, admissionId);
    }
    long exists = admissionFeeAuditMapper.selectCount(query);
    if (exists > 0) {
      throw new IllegalStateException("存在待审核的入住费用单，请先完成审核");
    }
  }

  private void ensureNoPendingDischargeAudit(Long orgId, Long elderId, Long dischargeApplyId) {
    var query = Wrappers.lambdaQuery(DischargeFeeAudit.class)
        .eq(DischargeFeeAudit::getIsDeleted, 0)
        .eq(orgId != null, DischargeFeeAudit::getOrgId, orgId)
        .eq(DischargeFeeAudit::getElderId, elderId)
        .eq(DischargeFeeAudit::getStatus, FeeWorkflowConstants.AUDIT_PENDING);
    if (dischargeApplyId != null) {
      query.eq(DischargeFeeAudit::getDischargeApplyId, dischargeApplyId);
    }
    long exists = dischargeFeeAuditMapper.selectCount(query);
    if (exists > 0) {
      throw new IllegalStateException("存在待审核的退住费用单，请先完成审核");
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

  private String runDailyReconcile(Long orgId) {
    try {
      ReconcileResponse response = financeService.reconcile(orgId, LocalDate.now());
      String mismatch = response.isMismatch() ? "有异常" : "无异常";
      return response.getDate() + " 收款" + money(response.getTotalReceived()) + "，" + mismatch;
    } catch (RuntimeException exception) {
      return "失败(" + exception.getMessage() + ")";
    }
  }

  private String buildFeeSnapshot(DischargeSettlement settlement, SettlementAmounts amounts) {
    String feeItem = hasText(settlement.getFeeItem()) ? settlement.getFeeItem() : "未配置";
    String config = hasText(settlement.getDischargeFeeConfig()) ? settlement.getDischargeFeeConfig() : "未配置";
    return "费用项=" + feeItem
        + "，配置=" + config
        + "，应收=" + money(settlement.getPayableAmount())
        + "，押金抵扣=" + money(amounts.fromDepositAmount())
        + "，退款=" + money(amounts.refundAmount())
        + "，补缴=" + money(amounts.supplementAmount());
  }

  private String buildDepositSummary(SettlementAmounts amounts) {
    if (amounts.fromDepositAmount().compareTo(BigDecimal.ZERO) == 0
        && amounts.refundAmount().compareTo(BigDecimal.ZERO) == 0
        && amounts.supplementAmount().compareTo(BigDecimal.ZERO) == 0) {
      return "无押金变动";
    }
    if (amounts.supplementAmount().compareTo(BigDecimal.ZERO) > 0) {
      return "余额不足，需补缴" + money(amounts.supplementAmount());
    }
    if (amounts.refundAmount().compareTo(BigDecimal.ZERO) > 0) {
      return "押金抵扣" + money(amounts.fromDepositAmount()) + "，应退" + money(amounts.refundAmount());
    }
    return "押金抵扣" + money(amounts.fromDepositAmount()) + "，无需退款";
  }

  private String releaseBedAndSyncElder(Long orgId, Long elderId) {
    ElderProfile elder = elderMapper.selectOne(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(orgId != null, ElderProfile::getOrgId, orgId)
        .eq(ElderProfile::getId, elderId)
        .last("FOR UPDATE"));
    if (elder == null) {
      return "老人不存在";
    }

    String bedRelease = "无需释放";
    if (elder.getBedId() != null) {
      Bed bed = bedMapper.selectById(elder.getBedId());
      if (bed != null
          && !Integer.valueOf(1).equals(bed.getIsDeleted())
          && (orgId == null || Objects.equals(bed.getOrgId(), orgId))) {
        bed.setElderId(null);
        bed.setStatus(1);
        bedMapper.updateById(bed);
        bedRelease = "已释放床位#" + bed.getId();
      } else {
        bedRelease = "床位不存在或不在本机构";
      }
      elder.setBedId(null);
    }

    if (elder.getStatus() == null || elder.getStatus() != 3) {
      elder.setStatus(3);
    }
    elderMapper.updateById(elder);
    return bedRelease + "，老人状态已回写离院";
  }

  private String mergeRemark(String origin, String append) {
    String base = normalizeOptionalText(origin);
    String extra = normalizeOptionalText(append);
    if (!hasText(base)) {
      return truncateRemark(extra);
    }
    if (!hasText(extra)) {
      return truncateRemark(base);
    }
    return truncateRemark(base + "；" + extra);
  }

  private String truncateRemark(String remark) {
    String normalized = normalizeOptionalText(remark);
    if (!hasText(normalized)) {
      return null;
    }
    return normalized.length() <= 255 ? normalized : normalized.substring(0, 255);
  }

  private String money(BigDecimal amount) {
    BigDecimal value = amount == null ? BigDecimal.ZERO : amount;
    return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
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

  private String generateDetailNo(Long elderId) {
    long tail = elderId == null ? 0L : Math.abs(elderId % 1000);
    return "DS" + System.currentTimeMillis() + String.format("%03d", tail);
  }

  private String normalizeConsumptionCategory(String category) {
    if (!hasText(category)) {
      return null;
    }
    String normalized = category.trim().toUpperCase(Locale.ROOT);
    return switch (normalized) {
      case "DINING", "餐饮", "餐饮消费" -> "DINING";
      case "BED", "床位", "床位消费" -> "BED";
      case "NURSING", "护理", "护理消费" -> "NURSING";
      case "DEPOSIT", "押金", "押金消费" -> "DEPOSIT";
      case "MEDICINE", "药品", "药品消费" -> "MEDICINE";
      case "OTHER", "其他", "其他消费" -> "OTHER";
      default -> normalized;
    };
  }

  private String normalizeOptionalText(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private LocalDate parseDateOrNull(String rawDate, String fieldName) {
    if (!hasText(rawDate)) {
      return null;
    }
    try {
      return LocalDate.parse(rawDate);
    } catch (Exception exception) {
      throw new IllegalArgumentException(fieldName + " 格式错误，期望 YYYY-MM-DD");
    }
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
