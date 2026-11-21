package com.zhiyangyun.care.finance.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.finance.entity.ConsumptionRecord;
import com.zhiyangyun.care.finance.entity.DischargeSettlement;
import com.zhiyangyun.care.finance.entity.FinanceRefundVoucher;
import com.zhiyangyun.care.finance.entity.PaymentRecord;
import com.zhiyangyun.care.finance.entity.ReconciliationDaily;
import com.zhiyangyun.care.finance.mapper.ConsumptionRecordMapper;
import com.zhiyangyun.care.finance.mapper.DischargeSettlementMapper;
import com.zhiyangyun.care.finance.mapper.FinanceRefundVoucherMapper;
import com.zhiyangyun.care.finance.mapper.PaymentRecordMapper;
import com.zhiyangyun.care.finance.mapper.ReconciliationDailyMapper;
import com.zhiyangyun.care.finance.model.BillDeductionPreviewResponse;
import com.zhiyangyun.care.finance.model.PaymentRequest;
import com.zhiyangyun.care.finance.model.PaymentResponse;
import com.zhiyangyun.care.finance.model.PaymentVoucherUse;
import com.zhiyangyun.care.finance.model.ReconcileResponse;
import com.zhiyangyun.care.finance.service.FinanceMonthLockService;
import com.zhiyangyun.care.finance.service.FinanceService;
import com.zhiyangyun.care.finance.service.FinanceVoucherService;
import com.zhiyangyun.care.ltci.entity.LtciSettlement;
import com.zhiyangyun.care.ltci.mapper.LtciSettlementMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinanceServiceImpl implements FinanceService {
  private static final DateTimeFormatter LTCI_MONTH_FMT = DateTimeFormatter.ofPattern("yyyyMM");

  private final BillMonthlyMapper billMonthlyMapper;
  private final PaymentRecordMapper paymentRecordMapper;
  private final ReconciliationDailyMapper reconciliationDailyMapper;
  private final ConsumptionRecordMapper consumptionRecordMapper;
  private final FinanceRefundVoucherMapper financeRefundVoucherMapper;
  private final DischargeSettlementMapper dischargeSettlementMapper;
  private final ElderMapper elderMapper;
  private final FinanceMonthLockService financeMonthLockService;
  private final FinanceVoucherService financeVoucherService;
  private final LtciSettlementMapper ltciSettlementMapper;

  public FinanceServiceImpl(BillMonthlyMapper billMonthlyMapper,
      PaymentRecordMapper paymentRecordMapper,
      ReconciliationDailyMapper reconciliationDailyMapper,
      ConsumptionRecordMapper consumptionRecordMapper,
      FinanceRefundVoucherMapper financeRefundVoucherMapper,
      DischargeSettlementMapper dischargeSettlementMapper,
      ElderMapper elderMapper,
      FinanceMonthLockService financeMonthLockService,
      FinanceVoucherService financeVoucherService,
      LtciSettlementMapper ltciSettlementMapper) {
    this.billMonthlyMapper = billMonthlyMapper;
    this.paymentRecordMapper = paymentRecordMapper;
    this.reconciliationDailyMapper = reconciliationDailyMapper;
    this.consumptionRecordMapper = consumptionRecordMapper;
    this.financeRefundVoucherMapper = financeRefundVoucherMapper;
    this.dischargeSettlementMapper = dischargeSettlementMapper;
    this.elderMapper = elderMapper;
    this.financeMonthLockService = financeMonthLockService;
    this.financeVoucherService = financeVoucherService;
    this.ltciSettlementMapper = ltciSettlementMapper;
  }

  @Override
  @Transactional
  public PaymentResponse pay(Long billId, PaymentRequest request, Long operatorStaffId) {
    BigDecimal cash = scaleAmount(request.getAmount());
    BigDecimal ltciDeduct = scaleAmount(request.getLtciDeductAmount());
    BigDecimal discount = scaleAmount(request.getDiscountAmount());
    List<PaymentVoucherUse> voucherUses = request.getVoucherUses() == null
        ? List.of()
        : request.getVoucherUses();
    BigDecimal voucherTotal = sumVoucherUses(voucherUses);
    assertNonNegativeDeductions(cash, ltciDeduct, discount);
    BigDecimal settleTotal = cash.add(ltciDeduct).add(discount).add(voucherTotal);
    if (settleTotal.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("实收金额与抵扣金额不能同时为 0");
    }
    BillMonthly bill = findBillForUpdate(billId);
    if (bill == null) {
      throw new IllegalArgumentException("Bill not found");
    }
    ensureOrgAccess(bill.getOrgId());
    if (Integer.valueOf(9).equals(bill.getStatus())) {
      throw new IllegalStateException("Invalid bill can not be paid");
    }
    financeMonthLockService.assertMonthEditable(bill.getOrgId(), YearMonth.parse(bill.getBillMonth()), "收款登记");

    String externalTxnId = normalizeExternalTxnId(request.getExternalTxnId());
    if (externalTxnId != null) {
      PaymentRecord existing = paymentRecordMapper.selectOne(
          Wrappers.lambdaQuery(PaymentRecord.class)
              .eq(PaymentRecord::getIsDeleted, 0)
              .eq(PaymentRecord::getOrgId, bill.getOrgId())
              .eq(PaymentRecord::getExternalTxnId, externalTxnId)
              .last("LIMIT 1"));
      if (existing != null) {
        if (!Objects.equals(existing.getBillMonthlyId(), billId)) {
          throw new IllegalStateException("External transaction already belongs to another bill");
        }
        return toResponse(bill);
      }
    }

    BigDecimal paidAmount = bill.getPaidAmount() == null ? BigDecimal.ZERO : bill.getPaidAmount();
    BigDecimal totalAmount = bill.getTotalAmount() == null ? BigDecimal.ZERO : bill.getTotalAmount();
    BigDecimal outstanding = totalAmount.subtract(paidAmount);

    if (settleTotal.compareTo(outstanding) > 0) {
      throw new IllegalArgumentException("实收与抵扣合计超过应收余额，请核对后重新登记");
    }
    assertDiscountReason(discount, request.getDiscountReason());
    assertLtciDeductWithinQuota(bill, ltciDeduct, null);

    PaymentRecord record = new PaymentRecord();
    record.setOrgId(bill.getOrgId());
    record.setBillMonthlyId(bill.getId());
    record.setAmount(cash);
    record.setPayableAmount(outstanding);
    record.setLtciDeductAmount(ltciDeduct);
    record.setDiscountAmount(discount);
    record.setDiscountReason(trimToNull(request.getDiscountReason()));
    record.setVoucherAmount(voucherTotal);
    record.setSettledAmount(settleTotal);
    record.setPayMethod(safeMethod(request.getMethod()));
    record.setExternalTxnId(externalTxnId);
    record.setPaidAt(request.getPaidAt());
    record.setOperatorStaffId(operatorStaffId);
    record.setRemark(request.getRemark());
    try {
      paymentRecordMapper.insert(record);
    } catch (DuplicateKeyException ex) {
      if (externalTxnId != null) {
        PaymentRecord existing = paymentRecordMapper.selectOne(
            Wrappers.lambdaQuery(PaymentRecord.class)
                .eq(PaymentRecord::getIsDeleted, 0)
                .eq(PaymentRecord::getOrgId, bill.getOrgId())
                .eq(PaymentRecord::getExternalTxnId, externalTxnId)
                .last("LIMIT 1"));
        if (existing != null && Objects.equals(existing.getBillMonthlyId(), billId)) {
          BillMonthly latestBill = findBillForUpdate(billId);
          if (latestBill == null) {
            throw new IllegalStateException("Payment exists but bill is missing");
          }
          return toResponse(latestBill);
        }
        if (existing != null) {
          throw new IllegalStateException("External transaction already belongs to another bill", ex);
        }
      }
      throw ex;
    }
    financeVoucherService.consume(
        voucherUses,
        bill.getOrgId(),
        bill.getElderId(),
        totalAmount,
        bill.getId(),
        record.getId(),
        operatorStaffId);
    upsertPaymentConsumptionRecord(record, bill);

    BigDecimal newPaid = paidAmount.add(settleTotal);
    BigDecimal newOutstanding = totalAmount.subtract(newPaid);
    bill.setPaidAmount(newPaid);
    bill.setOutstandingAmount(newOutstanding);
    if (newOutstanding.compareTo(BigDecimal.ZERO) == 0) {
      bill.setStatus(2);
    } else {
      bill.setStatus(1);
    }
    billMonthlyMapper.updateById(bill);

    return withBreakdown(toResponse(bill), cash, ltciDeduct, discount, voucherTotal, settleTotal);
  }

  @Override
  @Transactional
  public PaymentResponse updatePaymentRecord(Long paymentRecordId, PaymentRequest request, Long operatorStaffId) {
    BigDecimal cash = scaleAmount(request.getAmount());
    BigDecimal ltciDeduct = scaleAmount(request.getLtciDeductAmount());
    BigDecimal discount = scaleAmount(request.getDiscountAmount());
    List<PaymentVoucherUse> voucherUses = request.getVoucherUses() == null
        ? List.of()
        : request.getVoucherUses();
    BigDecimal voucherTotal = sumVoucherUses(voucherUses);
    assertNonNegativeDeductions(cash, ltciDeduct, discount);
    BigDecimal settleTotal = cash.add(ltciDeduct).add(discount).add(voucherTotal);
    if (settleTotal.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("实收金额与抵扣金额不能同时为 0");
    }
    PaymentRecord paymentRecord = findPaymentRecordForUpdate(paymentRecordId);
    if (paymentRecord == null) {
      throw new IllegalArgumentException("Payment record not found");
    }
    BillMonthly bill = findBillForUpdate(paymentRecord.getBillMonthlyId());
    if (bill == null) {
      throw new IllegalArgumentException("Bill not found");
    }
    ensureOrgAccess(bill.getOrgId());
    if (paymentRecord.getOrgId() != null && !Objects.equals(paymentRecord.getOrgId(), bill.getOrgId())) {
      throw new IllegalStateException("Payment record org mismatch");
    }
    if (Integer.valueOf(9).equals(bill.getStatus())) {
      throw new IllegalStateException("Invalid bill can not edit payment");
    }
    financeMonthLockService.assertMonthEditable(bill.getOrgId(), YearMonth.parse(bill.getBillMonth()), "修改收款");
    String externalTxnId = normalizeExternalTxnId(request.getExternalTxnId());
    if (externalTxnId != null) {
      PaymentRecord existing = paymentRecordMapper.selectOne(
          Wrappers.lambdaQuery(PaymentRecord.class)
              .eq(PaymentRecord::getIsDeleted, 0)
              .eq(PaymentRecord::getOrgId, bill.getOrgId())
              .eq(PaymentRecord::getExternalTxnId, externalTxnId)
              .ne(PaymentRecord::getId, paymentRecordId)
              .last("LIMIT 1"));
      if (existing != null) {
        throw new IllegalStateException("External transaction already exists");
      }
    }
    BigDecimal totalAmount = bill.getTotalAmount() == null ? BigDecimal.ZERO : bill.getTotalAmount();
    BigDecimal otherSettled = paymentRecordMapper.selectList(
            Wrappers.lambdaQuery(PaymentRecord.class)
                .eq(PaymentRecord::getIsDeleted, 0)
                .eq(PaymentRecord::getBillMonthlyId, bill.getId())
                .ne(PaymentRecord::getId, paymentRecordId))
        .stream()
        .map(FinanceServiceImpl::settledOf)
        .filter(amount -> amount.compareTo(BigDecimal.ZERO) > 0)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal newPaid = otherSettled.add(settleTotal);
    if (newPaid.compareTo(totalAmount) > 0) {
      throw new IllegalArgumentException("实收与抵扣合计超过应收余额，请核对后重新登记");
    }
    assertDiscountReason(discount, request.getDiscountReason());
    assertLtciDeductWithinQuota(bill, ltciDeduct, paymentRecordId);

    // 券额度先按原记录全额退回，再按本次登记重新核销，避免改小金额后额度被占死
    financeVoucherService.release(paymentRecordId, operatorStaffId, "修改收款重算");
    financeVoucherService.consume(
        voucherUses,
        bill.getOrgId(),
        bill.getElderId(),
        totalAmount,
        bill.getId(),
        paymentRecordId,
        operatorStaffId);

    String beforeMethod = safeMethod(paymentRecord.getPayMethod());
    String nextMethod = safeMethod(request.getMethod());
    paymentRecord.setAmount(cash);
    paymentRecord.setPayableAmount(totalAmount.subtract(otherSettled));
    paymentRecord.setLtciDeductAmount(ltciDeduct);
    paymentRecord.setDiscountAmount(discount);
    paymentRecord.setDiscountReason(trimToNull(request.getDiscountReason()));
    paymentRecord.setVoucherAmount(voucherTotal);
    paymentRecord.setSettledAmount(settleTotal);
    paymentRecord.setPayMethod(nextMethod);
    paymentRecord.setExternalTxnId(externalTxnId);
    paymentRecord.setPaidAt(request.getPaidAt());
    paymentRecord.setRemark(mergePaymentRemark(request.getRemark(), beforeMethod, nextMethod));
    paymentRecord.setOperatorStaffId(operatorStaffId);
    try {
      paymentRecordMapper.updateById(paymentRecord);
    } catch (DuplicateKeyException ex) {
      if (externalTxnId != null) {
        PaymentRecord existing = paymentRecordMapper.selectOne(
            Wrappers.lambdaQuery(PaymentRecord.class)
                .eq(PaymentRecord::getIsDeleted, 0)
                .eq(PaymentRecord::getOrgId, bill.getOrgId())
                .eq(PaymentRecord::getExternalTxnId, externalTxnId)
                .ne(PaymentRecord::getId, paymentRecordId)
                .last("LIMIT 1"));
        if (existing != null) {
          throw new IllegalStateException("External transaction already exists", ex);
        }
      }
      throw ex;
    }

    BigDecimal newOutstanding = totalAmount.subtract(newPaid);
    bill.setPaidAmount(newPaid);
    bill.setOutstandingAmount(newOutstanding);
    bill.setStatus(newOutstanding.compareTo(BigDecimal.ZERO) == 0 ? 2 : (newPaid.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0));
    billMonthlyMapper.updateById(bill);
    upsertPaymentConsumptionRecord(paymentRecord, bill);
    return withBreakdown(toResponse(bill), cash, ltciDeduct, discount, voucherTotal, settleTotal);
  }

  @Override
  @Transactional
  public ReconcileResponse reconcile(Long orgId, LocalDate date) {
    ensureOrgAccess(orgId);
    LocalDateTime start = date.atStartOfDay();
    LocalDateTime end = date.plusDays(1).atStartOfDay();

    List<PaymentRecord> records = paymentRecordMapper.selectList(
        Wrappers.lambdaQuery(PaymentRecord.class)
            .eq(PaymentRecord::getIsDeleted, 0)
            .eq(PaymentRecord::getOrgId, orgId)
            .ge(PaymentRecord::getPaidAt, start)
            .lt(PaymentRecord::getPaidAt, end));
    List<Long> billIds = records.stream()
        .map(PaymentRecord::getBillMonthlyId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, BillMonthly> billMap = billIds.isEmpty()
        ? Map.of()
        : billMonthlyMapper.selectList(
            Wrappers.lambdaQuery(BillMonthly.class)
                .eq(BillMonthly::getIsDeleted, 0)
                .in(BillMonthly::getId, billIds))
            .stream()
            .collect(java.util.stream.Collectors.toMap(BillMonthly::getId, item -> item, (a, b) -> a));

    BigDecimal total = BigDecimal.ZERO;
    for (PaymentRecord record : records) {
      total = total.add(record.getAmount());
    }
    BigDecimal totalRefund = sumRefundAmount(orgId, start, end);
    BigDecimal netReceived = total.subtract(totalRefund);
    boolean mismatch = hasReconcileMismatch(orgId, records, billMap, start, end);
    String remark = buildReconcileRemark(orgId, records, billMap, mismatch, start, end, totalRefund);

    List<ReconciliationDaily> existingList = reconciliationDailyMapper.selectList(
        Wrappers.lambdaQuery(ReconciliationDaily.class)
            .eq(ReconciliationDaily::getOrgId, orgId)
            .eq(ReconciliationDaily::getReconcileDate, date)
            .eq(ReconciliationDaily::getIsDeleted, 0)
            .orderByDesc(ReconciliationDaily::getId)
            .last("FOR UPDATE"));
    ReconciliationDaily daily;
    if (existingList == null || existingList.isEmpty()) {
      daily = new ReconciliationDaily();
      daily.setOrgId(orgId);
      daily.setReconcileDate(date);
      daily.setTotalReceived(total);
      daily.setTotalRefund(totalRefund);
      daily.setNetReceived(netReceived);
      daily.setMismatchFlag(mismatch ? 1 : 0);
      daily.setRemark(remark);
      try {
        reconciliationDailyMapper.insert(daily);
      } catch (DuplicateKeyException ex) {
        daily = reconciliationDailyMapper.selectOne(
            Wrappers.lambdaQuery(ReconciliationDaily.class)
                .eq(ReconciliationDaily::getOrgId, orgId)
                .eq(ReconciliationDaily::getReconcileDate, date)
                .eq(ReconciliationDaily::getIsDeleted, 0)
                .last("LIMIT 1 FOR UPDATE"));
        if (daily == null) {
          throw ex;
        }
        daily.setTotalReceived(total);
        daily.setTotalRefund(totalRefund);
        daily.setNetReceived(netReceived);
        daily.setMismatchFlag(mismatch ? 1 : 0);
        daily.setRemark(remark);
        reconciliationDailyMapper.updateById(daily);
      }
    } else {
      daily = existingList.get(0);
      daily.setTotalReceived(total);
      daily.setTotalRefund(totalRefund);
      daily.setNetReceived(netReceived);
      daily.setMismatchFlag(mismatch ? 1 : 0);
      daily.setRemark(remark);
      reconciliationDailyMapper.updateById(daily);
      for (int i = 1; i < existingList.size(); i++) {
        ReconciliationDaily duplicate = existingList.get(i);
        duplicate.setIsDeleted(1);
        reconciliationDailyMapper.updateById(duplicate);
      }
    }

    ReconcileResponse response = new ReconcileResponse();
    response.setDate(date);
    response.setTotalReceived(total);
    response.setTotalRefund(totalRefund);
    response.setNetReceived(netReceived);
    response.setMismatchFlag(mismatch ? 1 : 0);
    response.setMismatch(mismatch);
    response.setRemark(remark);
    return response;
  }

  @Override
  @Transactional
  public void invalidateBill(Long billId, Long operatorStaffId) {
    BillMonthly bill = findBillForUpdate(billId);
    if (bill == null) {
      throw new IllegalArgumentException("Bill not found");
    }
    ensureOrgAccess(bill.getOrgId());
    if (Integer.valueOf(9).equals(bill.getStatus())) {
      return;
    }
    financeMonthLockService.assertMonthEditable(bill.getOrgId(), YearMonth.parse(bill.getBillMonth()), "作废账单");
    Long paymentCount = paymentRecordMapper.selectCount(
        Wrappers.lambdaQuery(PaymentRecord.class)
            .eq(PaymentRecord::getIsDeleted, 0)
            .eq(PaymentRecord::getBillMonthlyId, billId));
    if (paymentCount != null && paymentCount > 0) {
      throw new IllegalStateException("Bill has payment records, refund/reversal is required before invalidation");
    }
    bill.setStatus(9);
    bill.setOutstandingAmount(BigDecimal.ZERO);
    billMonthlyMapper.updateById(bill);
  }

  private static BigDecimal scaleAmount(BigDecimal value) {
    return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
  }

  private static BigDecimal settledOf(PaymentRecord record) {
    if (record == null) {
      return BigDecimal.ZERO;
    }
    // 历史数据没有 settled_amount，退回到实收金额口径
    return scaleAmount(record.getSettledAmount() == null ? record.getAmount() : record.getSettledAmount());
  }

  private static BigDecimal sumVoucherUses(List<PaymentVoucherUse> uses) {
    if (uses == null || uses.isEmpty()) {
      return BigDecimal.ZERO;
    }
    return uses.stream()
        .filter(Objects::nonNull)
        .map(item -> scaleAmount(item.getAmount()))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private static void assertNonNegativeDeductions(BigDecimal cash, BigDecimal ltciDeduct, BigDecimal discount) {
    if (cash.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("实收金额不能为负数");
    }
    if (ltciDeduct.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("长护险抵扣不能为负数");
    }
    if (discount.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("折扣减免不能为负数");
    }
  }

  private static void assertDiscountReason(BigDecimal discount, String reason) {
    if (discount.compareTo(BigDecimal.ZERO) > 0 && trimToNull(reason) == null) {
      throw new IllegalArgumentException("填写折扣减免金额时必须说明减免原因");
    }
  }

  private static String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  /**
   * 长护险抵扣不得超过本月统筹支付额度。仅在存在长护险结算单时限额，
   * 未接入长护险结算的机构可按线下口径手工登记。
   */
  private void assertLtciDeductWithinQuota(BillMonthly bill, BigDecimal ltciDeduct, Long excludePaymentRecordId) {
    if (ltciDeduct.compareTo(BigDecimal.ZERO) <= 0) {
      return;
    }
    BigDecimal fundPay = resolveLtciFundPay(bill);
    if (fundPay == null) {
      return;
    }
    BigDecimal used = ltciDeductUsed(bill.getId(), excludePaymentRecordId);
    BigDecimal available = fundPay.subtract(used);
    if (ltciDeduct.compareTo(available) > 0) {
      throw new IllegalArgumentException("长护险抵扣超过本月统筹额度，剩余可抵扣 "
          + available.max(BigDecimal.ZERO) + " 元");
    }
  }

  /** 本月长护险统筹支付额（元）；无结算单返回 null 表示不限额。 */
  private BigDecimal resolveLtciFundPay(BillMonthly bill) {
    if (bill == null || bill.getElderId() == null || bill.getBillMonth() == null) {
      return null;
    }
    String settleMonth;
    try {
      settleMonth = YearMonth.parse(bill.getBillMonth()).format(LTCI_MONTH_FMT);
    } catch (Exception ignored) {
      return null;
    }
    LtciSettlement settlement = ltciSettlementMapper.selectOne(
        Wrappers.lambdaQuery(LtciSettlement.class)
            .eq(LtciSettlement::getIsDeleted, 0)
            .eq(bill.getOrgId() != null, LtciSettlement::getOrgId, bill.getOrgId())
            .eq(LtciSettlement::getElderId, bill.getElderId())
            .eq(LtciSettlement::getSettleMonth, settleMonth)
            .last("LIMIT 1"));
    if (settlement == null || settlement.getFundPay() == null) {
      return null;
    }
    // ltci_settlement 金额单位为分
    return BigDecimal.valueOf(settlement.getFundPay())
        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
  }

  private BigDecimal ltciDeductUsed(Long billId, Long excludePaymentRecordId) {
    if (billId == null) {
      return BigDecimal.ZERO;
    }
    var wrapper = Wrappers.lambdaQuery(PaymentRecord.class)
        .eq(PaymentRecord::getIsDeleted, 0)
        .eq(PaymentRecord::getBillMonthlyId, billId);
    if (excludePaymentRecordId != null) {
      wrapper.ne(PaymentRecord::getId, excludePaymentRecordId);
    }
    return paymentRecordMapper.selectList(wrapper).stream()
        .map(item -> scaleAmount(item.getLtciDeductAmount()))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  @Override
  public BillDeductionPreviewResponse deductionPreview(Long billId) {
    BillMonthly bill = billId == null ? null : billMonthlyMapper.selectOne(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getId, billId)
            .eq(BillMonthly::getIsDeleted, 0)
            .last("LIMIT 1"));
    if (bill == null) {
      throw new IllegalArgumentException("Bill not found");
    }
    ensureOrgAccess(bill.getOrgId());

    BigDecimal totalAmount = scaleAmount(bill.getTotalAmount());
    BigDecimal settled = scaleAmount(bill.getPaidAmount());
    BigDecimal outstanding = totalAmount.subtract(settled);

    BillDeductionPreviewResponse response = new BillDeductionPreviewResponse();
    response.setBillId(bill.getId());
    response.setElderId(bill.getElderId());
    ElderProfile elder = bill.getElderId() == null ? null : elderMapper.selectById(bill.getElderId());
    response.setElderName(elder == null ? null : elder.getFullName());
    response.setBillMonth(bill.getBillMonth());
    response.setTotalAmount(totalAmount);
    response.setSettledAmount(settled);
    response.setOutstandingAmount(outstanding.max(BigDecimal.ZERO));

    BigDecimal fundPay = resolveLtciFundPay(bill);
    BigDecimal ltciUsed = ltciDeductUsed(bill.getId(), null);
    response.setLtciUsedAmount(ltciUsed);
    if (fundPay == null) {
      response.setLtciFundPayAmount(BigDecimal.ZERO);
      response.setLtciAvailableAmount(response.getOutstandingAmount());
      response.setLtciHint("本月无长护险结算单，抵扣额度不限制，按线下结算口径手工登记");
    } else {
      response.setLtciFundPayAmount(fundPay);
      BigDecimal available = fundPay.subtract(ltciUsed).max(BigDecimal.ZERO);
      response.setLtciAvailableAmount(available.min(response.getOutstandingAmount()));
      response.setLtciHint("本月统筹支付 " + fundPay + " 元，已登记抵扣 " + ltciUsed + " 元");
    }

    response.setUsableVouchers(financeVoucherService.listUsable(bill.getElderId(), totalAmount, LocalDate.now()));
    return response;
  }

  private static PaymentResponse withBreakdown(
      PaymentResponse response,
      BigDecimal cash,
      BigDecimal ltciDeduct,
      BigDecimal discount,
      BigDecimal voucherAmount,
      BigDecimal settledAmount) {
    response.setCashAmount(cash);
    response.setLtciDeductAmount(ltciDeduct);
    response.setDiscountAmount(discount);
    response.setVoucherAmount(voucherAmount);
    response.setSettledAmount(settledAmount);
    return response;
  }

  private PaymentResponse toResponse(BillMonthly bill) {
    PaymentResponse response = new PaymentResponse();
    response.setBillId(bill.getId());
    response.setPaidAmount(bill.getPaidAmount());
    response.setOutstandingAmount(bill.getOutstandingAmount());
    response.setStatus(statusText(bill.getPaidAmount(), bill.getTotalAmount()));
    return response;
  }

  private String statusText(BigDecimal paid, BigDecimal total) {
    if (paid == null || paid.compareTo(BigDecimal.ZERO) == 0) {
      return "UNPAID";
    }
    if (total == null || paid.compareTo(total) < 0) {
      return "PARTIALLY_PAID";
    }
    return "PAID";
  }

  private void upsertPaymentConsumptionRecord(PaymentRecord paymentRecord, BillMonthly bill) {
    if (paymentRecord == null || bill == null || paymentRecord.getId() == null) {
      return;
    }
    ConsumptionRecord current = consumptionRecordMapper.selectOne(
        Wrappers.lambdaQuery(ConsumptionRecord.class)
            .eq(ConsumptionRecord::getIsDeleted, 0)
            .eq(ConsumptionRecord::getOrgId, bill.getOrgId())
            .eq(ConsumptionRecord::getSourceType, "BILL_PAYMENT")
            .eq(ConsumptionRecord::getSourceId, paymentRecord.getId())
            .last("LIMIT 1"));
    ElderProfile elder = bill.getElderId() == null ? null : elderMapper.selectById(bill.getElderId());
    if (current == null) {
      current = new ConsumptionRecord();
      // finance_consumption_record.tenant_id 是 NOT NULL 且无默认值，不显式赋值会直接插入失败
      current.setTenantId(bill.getOrgId());
      current.setOrgId(bill.getOrgId());
      current.setElderId(bill.getElderId());
      current.setSourceType("BILL_PAYMENT");
      current.setSourceId(paymentRecord.getId());
      current.setCategory("BILL_PAYMENT");
      current.setCreatedBy(paymentRecord.getOperatorStaffId());
    }
    current.setElderName(elder == null ? null : elder.getFullName());
    current.setConsumeDate(paymentRecord.getPaidAt() == null ? LocalDate.now() : paymentRecord.getPaidAt().toLocalDate());
    current.setAmount(paymentRecord.getAmount());
    current.setRemark(buildPaymentConsumptionRemark(bill, paymentRecord));
    if (current.getId() == null) {
      consumptionRecordMapper.insert(current);
      return;
    }
    consumptionRecordMapper.updateById(current);
  }

  private String buildPaymentConsumptionRemark(BillMonthly bill, PaymentRecord paymentRecord) {
    String billMonth = bill.getBillMonth() == null ? "-" : bill.getBillMonth();
    String contractNo = bill.getContractNoSnapshot() == null || bill.getContractNoSnapshot().isBlank()
        ? ""
        : (" 合同:" + bill.getContractNoSnapshot());
    String operatorRemark = paymentRecord.getRemark() == null || paymentRecord.getRemark().isBlank()
        ? ""
        : (" 备注:" + paymentRecord.getRemark().trim());
    return "账单收款 月份:" + billMonth + " 方式:" + safeMethod(paymentRecord.getPayMethod()) + contractNo + operatorRemark;
  }

  private String safeMethod(String method) {
    if (method == null || method.isBlank()) {
      return "CASH";
    }
    return method.trim().toUpperCase();
  }

  private String mergePaymentRemark(String requestRemark, String beforeMethod, String nextMethod) {
    String base = requestRemark == null ? "" : requestRemark.trim();
    if (beforeMethod == null || nextMethod == null || beforeMethod.equals(nextMethod)) {
      return base.isBlank() ? null : base;
    }
    String changed = "支付方式变更:" + beforeMethod + "->" + nextMethod;
    if (base.isBlank()) {
      return changed;
    }
    return base + "；" + changed;
  }

  private boolean hasReconcileMismatch(
      Long orgId,
      List<PaymentRecord> records,
      Map<Long, BillMonthly> billMap,
      LocalDateTime start,
      LocalDateTime end) {
    return !collectReconcileProblems(orgId, records, billMap, start, end).isEmpty();
  }

  private String buildReconcileRemark(
      Long orgId,
      List<PaymentRecord> records,
      Map<Long, BillMonthly> billMap,
      boolean mismatch,
      LocalDateTime start,
      LocalDateTime end,
      BigDecimal totalRefund) {
    List<String> problems = collectReconcileProblems(orgId, records, billMap, start, end);
    if (!mismatch || problems.isEmpty()) {
      return "对账正常；退款 " + totalRefund;
    }
    return String.join("；", problems);
  }

  private List<String> collectReconcileProblems(
      Long orgId,
      List<PaymentRecord> records,
      Map<Long, BillMonthly> billMap,
      LocalDateTime start,
      LocalDateTime end) {
    List<String> problems = new ArrayList<>();
    Map<String, Integer> duplicateCountMap = new HashMap<>();
    for (PaymentRecord record : records) {
      String signature = reconcileSignature(record);
      duplicateCountMap.merge(signature, 1, Integer::sum);
    }
    long duplicateCount = records.stream()
        .filter(record -> duplicateCountMap.getOrDefault(reconcileSignature(record), 0) > 1)
        .count();
    if (duplicateCount > 0) {
      problems.add("疑似重复收款 " + duplicateCount + " 笔");
    }
    long missingBillCount = records.stream()
        .filter(record -> record.getBillMonthlyId() == null || !billMap.containsKey(record.getBillMonthlyId()))
        .count();
    if (missingBillCount > 0) {
      problems.add("存在未关联账单收款 " + missingBillCount + " 笔");
    }
    long invalidBillCount = records.stream()
        .map(record -> billMap.get(record.getBillMonthlyId()))
        .filter(Objects::nonNull)
        .filter(bill -> Integer.valueOf(9).equals(bill.getStatus()))
        .count();
    if (invalidBillCount > 0) {
      problems.add("无效账单仍有收款 " + invalidBillCount + " 笔");
    }
    long overpaidBillCount = billMap.values().stream()
        .filter(Objects::nonNull)
        .filter(bill -> {
          BigDecimal totalAmount = bill.getTotalAmount() == null ? BigDecimal.ZERO : bill.getTotalAmount();
          BigDecimal paidAmount = bill.getPaidAmount() == null ? BigDecimal.ZERO : bill.getPaidAmount();
          BigDecimal outstanding = bill.getOutstandingAmount() == null ? BigDecimal.ZERO : bill.getOutstandingAmount();
          return paidAmount.compareTo(totalAmount) > 0 || outstanding.compareTo(BigDecimal.ZERO) < 0;
        })
        .count();
    if (overpaidBillCount > 0) {
      problems.add("账单金额状态异常 " + overpaidBillCount + " 笔");
    }
    long refundWithoutVoucherCount = dischargeSettlementMapper.selectCount(
        Wrappers.lambdaQuery(DischargeSettlement.class)
            .eq(DischargeSettlement::getIsDeleted, 0)
            .eq(orgId != null, DischargeSettlement::getOrgId, orgId)
            .eq(DischargeSettlement::getFinanceRefunded, 1)
            .isNull(DischargeSettlement::getRefundVoucherId)
            .ge(DischargeSettlement::getFinanceRefundTime, start)
            .lt(DischargeSettlement::getFinanceRefundTime, end));
    if (refundWithoutVoucherCount > 0) {
      problems.add("退款缺少凭证 " + refundWithoutVoucherCount + " 笔");
    }
    long invalidRefundCount = financeRefundVoucherMapper.selectCount(
        Wrappers.lambdaQuery(FinanceRefundVoucher.class)
            .eq(FinanceRefundVoucher::getIsDeleted, 0)
            .eq(orgId != null, FinanceRefundVoucher::getOrgId, orgId)
            .eq(FinanceRefundVoucher::getStatus, "PAID")
            .ge(FinanceRefundVoucher::getExecutedAt, start)
            .lt(FinanceRefundVoucher::getExecutedAt, end)
            .le(FinanceRefundVoucher::getAmount, BigDecimal.ZERO));
    if (invalidRefundCount > 0) {
      problems.add("退款金额异常 " + invalidRefundCount + " 笔");
    }
    return problems;
  }

  private String reconcileSignature(PaymentRecord record) {
    if (record == null) {
      return "NULL";
    }
    return String.valueOf(record.getBillMonthlyId()) + "|"
        + String.valueOf(record.getAmount()) + "|"
        + safeMethod(record.getPayMethod()) + "|"
        + String.valueOf(record.getPaidAt());
  }

  private BillMonthly findBillForUpdate(Long billId) {
    if (billId == null) {
      return null;
    }
    return billMonthlyMapper.selectOne(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getId, billId)
            .eq(BillMonthly::getIsDeleted, 0)
            .last("LIMIT 1 FOR UPDATE"));
  }

  private PaymentRecord findPaymentRecordForUpdate(Long paymentRecordId) {
    if (paymentRecordId == null) {
      return null;
    }
    return paymentRecordMapper.selectOne(
        Wrappers.lambdaQuery(PaymentRecord.class)
            .eq(PaymentRecord::getId, paymentRecordId)
            .eq(PaymentRecord::getIsDeleted, 0)
            .last("LIMIT 1 FOR UPDATE"));
  }

  private String normalizeExternalTxnId(String externalTxnId) {
    if (externalTxnId == null || externalTxnId.isBlank()) {
      return null;
    }
    String normalized = externalTxnId.trim();
    if (normalized.length() > 64) {
      throw new IllegalArgumentException("External transaction id is too long");
    }
    return normalized;
  }

  private void ensureOrgAccess(Long targetOrgId) {
    if (targetOrgId == null) {
      return;
    }
    Long currentOrgId = AuthContext.getOrgId();
    if (currentOrgId == null || AuthContext.isAdmin()) {
      return;
    }
    if (!Objects.equals(currentOrgId, targetOrgId)) {
      throw new org.springframework.security.access.AccessDeniedException("No permission to access another organization");
    }
  }

  private BigDecimal sumRefundAmount(Long orgId, LocalDateTime start, LocalDateTime end) {
    BigDecimal voucherAmount = financeRefundVoucherMapper.selectList(
            Wrappers.lambdaQuery(FinanceRefundVoucher.class)
                .eq(FinanceRefundVoucher::getIsDeleted, 0)
                .eq(orgId != null, FinanceRefundVoucher::getOrgId, orgId)
                .eq(FinanceRefundVoucher::getStatus, "PAID")
                .ge(FinanceRefundVoucher::getExecutedAt, start)
                .lt(FinanceRefundVoucher::getExecutedAt, end))
        .stream()
        .map(FinanceRefundVoucher::getAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal legacyAmount = dischargeSettlementMapper.selectList(
            Wrappers.lambdaQuery(DischargeSettlement.class)
                .eq(DischargeSettlement::getIsDeleted, 0)
                .eq(orgId != null, DischargeSettlement::getOrgId, orgId)
                .eq(DischargeSettlement::getFinanceRefunded, 1)
                .isNull(DischargeSettlement::getRefundVoucherId)
                .ge(DischargeSettlement::getFinanceRefundTime, start)
                .lt(DischargeSettlement::getFinanceRefundTime, end))
        .stream()
        .map(DischargeSettlement::getRefundAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    return voucherAmount.add(legacyAmount);
  }
}
