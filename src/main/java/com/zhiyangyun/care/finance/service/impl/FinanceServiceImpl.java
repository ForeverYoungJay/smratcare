package com.zhiyangyun.care.finance.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.finance.entity.ConsumptionRecord;
import com.zhiyangyun.care.finance.entity.PaymentRecord;
import com.zhiyangyun.care.finance.entity.ReconciliationDaily;
import com.zhiyangyun.care.finance.mapper.ConsumptionRecordMapper;
import com.zhiyangyun.care.finance.mapper.PaymentRecordMapper;
import com.zhiyangyun.care.finance.mapper.ReconciliationDailyMapper;
import com.zhiyangyun.care.finance.model.PaymentRequest;
import com.zhiyangyun.care.finance.model.PaymentResponse;
import com.zhiyangyun.care.finance.model.ReconcileResponse;
import com.zhiyangyun.care.finance.service.FinanceMonthLockService;
import com.zhiyangyun.care.finance.service.FinanceService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinanceServiceImpl implements FinanceService {
  private final BillMonthlyMapper billMonthlyMapper;
  private final PaymentRecordMapper paymentRecordMapper;
  private final ReconciliationDailyMapper reconciliationDailyMapper;
  private final ConsumptionRecordMapper consumptionRecordMapper;
  private final ElderMapper elderMapper;
  private final FinanceMonthLockService financeMonthLockService;

  public FinanceServiceImpl(BillMonthlyMapper billMonthlyMapper,
      PaymentRecordMapper paymentRecordMapper,
      ReconciliationDailyMapper reconciliationDailyMapper,
      ConsumptionRecordMapper consumptionRecordMapper,
      ElderMapper elderMapper,
      FinanceMonthLockService financeMonthLockService) {
    this.billMonthlyMapper = billMonthlyMapper;
    this.paymentRecordMapper = paymentRecordMapper;
    this.reconciliationDailyMapper = reconciliationDailyMapper;
    this.consumptionRecordMapper = consumptionRecordMapper;
    this.elderMapper = elderMapper;
    this.financeMonthLockService = financeMonthLockService;
  }

  @Override
  @Transactional
  public PaymentResponse pay(Long billId, PaymentRequest request, Long operatorStaffId) {
    if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Payment amount must be positive");
    }
    if (request.getExternalTxnId() != null && !request.getExternalTxnId().isBlank()) {
      PaymentRecord existing = paymentRecordMapper.selectOne(
          Wrappers.lambdaQuery(PaymentRecord.class)
              .eq(PaymentRecord::getBillMonthlyId, billId)
              .eq(PaymentRecord::getExternalTxnId, request.getExternalTxnId()));
      if (existing != null) {
        BillMonthly bill = billMonthlyMapper.selectById(existing.getBillMonthlyId());
        if (bill == null) {
          throw new IllegalStateException("Payment exists but bill is missing");
        }
        return toResponse(bill);
      }
    }

    BillMonthly bill = billMonthlyMapper.selectById(billId);
    if (bill == null) {
      throw new IllegalArgumentException("Bill not found");
    }
    if (Integer.valueOf(9).equals(bill.getStatus())) {
      throw new IllegalStateException("Invalid bill can not be paid");
    }
    financeMonthLockService.assertMonthEditable(bill.getOrgId(), YearMonth.parse(bill.getBillMonth()), "收款登记");

    BigDecimal paidAmount = bill.getPaidAmount() == null ? BigDecimal.ZERO : bill.getPaidAmount();
    BigDecimal totalAmount = bill.getTotalAmount() == null ? BigDecimal.ZERO : bill.getTotalAmount();
    BigDecimal outstanding = totalAmount.subtract(paidAmount);

    if (request.getAmount().compareTo(outstanding) > 0) {
      throw new IllegalArgumentException("Payment exceeds outstanding amount");
    }

    PaymentRecord record = new PaymentRecord();
    record.setOrgId(bill.getOrgId());
    record.setBillMonthlyId(bill.getId());
    record.setAmount(request.getAmount());
    record.setPayMethod(safeMethod(request.getMethod()));
    record.setExternalTxnId(request.getExternalTxnId());
    record.setPaidAt(request.getPaidAt());
    record.setOperatorStaffId(operatorStaffId);
    record.setRemark(request.getRemark());
    paymentRecordMapper.insert(record);
    upsertPaymentConsumptionRecord(record, bill);

    BigDecimal newPaid = paidAmount.add(request.getAmount());
    BigDecimal newOutstanding = totalAmount.subtract(newPaid);
    bill.setPaidAmount(newPaid);
    bill.setOutstandingAmount(newOutstanding);
    if (newOutstanding.compareTo(BigDecimal.ZERO) == 0) {
      bill.setStatus(2);
    } else {
      bill.setStatus(1);
    }
    billMonthlyMapper.updateById(bill);

    return toResponse(bill);
  }

  @Override
  @Transactional
  public PaymentResponse updatePaymentRecord(Long paymentRecordId, PaymentRequest request, Long operatorStaffId) {
    if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Payment amount must be positive");
    }
    PaymentRecord paymentRecord = paymentRecordMapper.selectById(paymentRecordId);
    if (paymentRecord == null || Integer.valueOf(1).equals(paymentRecord.getIsDeleted())) {
      throw new IllegalArgumentException("Payment record not found");
    }
    BillMonthly bill = billMonthlyMapper.selectById(paymentRecord.getBillMonthlyId());
    if (bill == null) {
      throw new IllegalArgumentException("Bill not found");
    }
    if (Integer.valueOf(9).equals(bill.getStatus())) {
      throw new IllegalStateException("Invalid bill can not edit payment");
    }
    financeMonthLockService.assertMonthEditable(bill.getOrgId(), YearMonth.parse(bill.getBillMonth()), "修改收款");
    BigDecimal totalAmount = bill.getTotalAmount() == null ? BigDecimal.ZERO : bill.getTotalAmount();
    BigDecimal otherPaid = paymentRecordMapper.selectList(
            Wrappers.lambdaQuery(PaymentRecord.class)
                .eq(PaymentRecord::getIsDeleted, 0)
                .eq(PaymentRecord::getBillMonthlyId, bill.getId())
                .ne(PaymentRecord::getId, paymentRecordId))
        .stream()
        .map(PaymentRecord::getAmount)
        .filter(amount -> amount != null && amount.compareTo(BigDecimal.ZERO) > 0)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal newPaid = otherPaid.add(request.getAmount());
    if (newPaid.compareTo(totalAmount) > 0) {
      throw new IllegalArgumentException("Payment exceeds outstanding amount");
    }

    String beforeMethod = safeMethod(paymentRecord.getPayMethod());
    String nextMethod = safeMethod(request.getMethod());
    paymentRecord.setAmount(request.getAmount());
    paymentRecord.setPayMethod(nextMethod);
    paymentRecord.setPaidAt(request.getPaidAt());
    paymentRecord.setRemark(mergePaymentRemark(request.getRemark(), beforeMethod, nextMethod));
    paymentRecord.setOperatorStaffId(operatorStaffId);
    paymentRecordMapper.updateById(paymentRecord);

    BigDecimal newOutstanding = totalAmount.subtract(newPaid);
    bill.setPaidAmount(newPaid);
    bill.setOutstandingAmount(newOutstanding);
    bill.setStatus(newOutstanding.compareTo(BigDecimal.ZERO) == 0 ? 2 : (newPaid.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0));
    billMonthlyMapper.updateById(bill);
    upsertPaymentConsumptionRecord(paymentRecord, bill);
    return toResponse(bill);
  }

  @Override
  @Transactional
  public ReconcileResponse reconcile(Long orgId, LocalDate date) {
    LocalDateTime start = date.atStartOfDay();
    LocalDateTime end = date.plusDays(1).atStartOfDay();

    List<PaymentRecord> records = paymentRecordMapper.selectList(
        Wrappers.lambdaQuery(PaymentRecord.class)
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
    boolean mismatch = hasReconcileMismatch(records, billMap);
    String remark = buildReconcileRemark(records, billMap, mismatch);

    List<ReconciliationDaily> existingList = reconciliationDailyMapper.selectList(
        Wrappers.lambdaQuery(ReconciliationDaily.class)
            .eq(ReconciliationDaily::getOrgId, orgId)
            .eq(ReconciliationDaily::getReconcileDate, date)
            .eq(ReconciliationDaily::getIsDeleted, 0)
            .orderByDesc(ReconciliationDaily::getId));
    ReconciliationDaily daily;
    if (existingList == null || existingList.isEmpty()) {
      daily = new ReconciliationDaily();
      daily.setOrgId(orgId);
      daily.setReconcileDate(date);
      daily.setTotalReceived(total);
      daily.setMismatchFlag(mismatch ? 1 : 0);
      daily.setRemark(remark);
      reconciliationDailyMapper.insert(daily);
    } else {
      daily = existingList.get(0);
      daily.setTotalReceived(total);
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
    response.setMismatchFlag(mismatch ? 1 : 0);
    response.setMismatch(mismatch);
    response.setRemark(remark);
    return response;
  }

  @Override
  @Transactional
  public void invalidateBill(Long billId, Long operatorStaffId) {
    BillMonthly bill = billMonthlyMapper.selectById(billId);
    if (bill == null) {
      throw new IllegalArgumentException("Bill not found");
    }
    if (Integer.valueOf(9).equals(bill.getStatus())) {
      return;
    }
    financeMonthLockService.assertMonthEditable(bill.getOrgId(), YearMonth.parse(bill.getBillMonth()), "作废账单");
    bill.setStatus(9);
    bill.setOutstandingAmount(BigDecimal.ZERO);
    billMonthlyMapper.updateById(bill);
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

  private boolean hasReconcileMismatch(List<PaymentRecord> records, Map<Long, BillMonthly> billMap) {
    return !collectReconcileProblems(records, billMap).isEmpty();
  }

  private String buildReconcileRemark(List<PaymentRecord> records, Map<Long, BillMonthly> billMap, boolean mismatch) {
    List<String> problems = collectReconcileProblems(records, billMap);
    if (!mismatch || problems.isEmpty()) {
      return "对账正常";
    }
    return String.join("；", problems);
  }

  private List<String> collectReconcileProblems(List<PaymentRecord> records, Map<Long, BillMonthly> billMap) {
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
}
