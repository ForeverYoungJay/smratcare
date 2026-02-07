package com.zhiyangyun.care.finance.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.finance.entity.PaymentRecord;
import com.zhiyangyun.care.finance.entity.ReconciliationDaily;
import com.zhiyangyun.care.finance.mapper.PaymentRecordMapper;
import com.zhiyangyun.care.finance.mapper.ReconciliationDailyMapper;
import com.zhiyangyun.care.finance.model.PaymentRequest;
import com.zhiyangyun.care.finance.model.PaymentResponse;
import com.zhiyangyun.care.finance.model.ReconcileResponse;
import com.zhiyangyun.care.finance.service.FinanceService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinanceServiceImpl implements FinanceService {
  private final BillMonthlyMapper billMonthlyMapper;
  private final PaymentRecordMapper paymentRecordMapper;
  private final ReconciliationDailyMapper reconciliationDailyMapper;

  public FinanceServiceImpl(BillMonthlyMapper billMonthlyMapper,
      PaymentRecordMapper paymentRecordMapper,
      ReconciliationDailyMapper reconciliationDailyMapper) {
    this.billMonthlyMapper = billMonthlyMapper;
    this.paymentRecordMapper = paymentRecordMapper;
    this.reconciliationDailyMapper = reconciliationDailyMapper;
  }

  @Override
  @Transactional
  public PaymentResponse pay(Long billId, PaymentRequest request, Long operatorStaffId) {
    if (request.getExternalTxnId() != null && !request.getExternalTxnId().isBlank()) {
      PaymentRecord existing = paymentRecordMapper.selectOne(
          Wrappers.lambdaQuery(PaymentRecord.class)
              .eq(PaymentRecord::getExternalTxnId, request.getExternalTxnId()));
      if (existing != null) {
        BillMonthly bill = billMonthlyMapper.selectById(existing.getBillMonthlyId());
        return toResponse(bill);
      }
    }

    BillMonthly bill = billMonthlyMapper.selectById(billId);
    if (bill == null) {
      throw new IllegalArgumentException("Bill not found");
    }

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
    record.setPayMethod(request.getMethod());
    record.setExternalTxnId(request.getExternalTxnId());
    record.setPaidAt(request.getPaidAt());
    record.setOperatorStaffId(operatorStaffId);
    record.setRemark(request.getRemark());
    paymentRecordMapper.insert(record);

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
  public ReconcileResponse reconcile(Long orgId, LocalDate date) {
    LocalDateTime start = date.atStartOfDay();
    LocalDateTime end = date.plusDays(1).atStartOfDay();

    List<PaymentRecord> records = paymentRecordMapper.selectList(
        Wrappers.lambdaQuery(PaymentRecord.class)
            .eq(PaymentRecord::getOrgId, orgId)
            .ge(PaymentRecord::getPaidAt, start)
            .lt(PaymentRecord::getPaidAt, end));

    BigDecimal total = BigDecimal.ZERO;
    for (PaymentRecord record : records) {
      total = total.add(record.getAmount());
    }

    ReconciliationDaily daily = reconciliationDailyMapper.selectOne(
        Wrappers.lambdaQuery(ReconciliationDaily.class)
            .eq(ReconciliationDaily::getOrgId, orgId)
            .eq(ReconciliationDaily::getReconcileDate, date));
    if (daily == null) {
      daily = new ReconciliationDaily();
      daily.setOrgId(orgId);
      daily.setReconcileDate(date);
    }
    daily.setTotalReceived(total);
    daily.setMismatchFlag(0);
    reconciliationDailyMapper.insert(daily);

    ReconcileResponse response = new ReconcileResponse();
    response.setDate(date);
    response.setTotalReceived(total);
    response.setMismatch(false);
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
}
