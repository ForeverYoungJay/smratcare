package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.finance.entity.FinanceRefundVoucher;
import com.zhiyangyun.care.finance.entity.ReconciliationDaily;
import com.zhiyangyun.care.finance.mapper.FinanceRefundVoucherMapper;
import com.zhiyangyun.care.finance.mapper.ReconciliationDailyMapper;
import com.zhiyangyun.care.finance.model.PaymentRequest;
import com.zhiyangyun.care.finance.service.FinanceService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FinanceReconcileTest {
  @Autowired
  private FinanceService financeService;

  @Autowired
  private BillMonthlyMapper billMonthlyMapper;

  @Autowired
  private ReconciliationDailyMapper reconciliationDailyMapper;

  @Autowired
  private FinanceRefundVoucherMapper financeRefundVoucherMapper;

  @Test
  void reconcile_counts_payments() {
    BillMonthly bill = new BillMonthly();
    bill.setOrgId(1L);
    bill.setElderId(210L);
    bill.setBillMonth("2026-01");
    bill.setTotalAmount(BigDecimal.valueOf(100));
    bill.setPaidAmount(BigDecimal.ZERO);
    bill.setOutstandingAmount(BigDecimal.valueOf(100));
    billMonthlyMapper.insert(bill);

    PaymentRequest request = new PaymentRequest();
    request.setAmount(BigDecimal.valueOf(30));
    request.setMethod("CASH");
    request.setPaidAt(LocalDateTime.now());

    financeService.pay(bill.getId(), request, 500L);

    FinanceRefundVoucher voucher = new FinanceRefundVoucher();
    voucher.setTenantId(1L);
    voucher.setOrgId(1L);
    voucher.setSettlementId(9001L);
    voucher.setElderId(210L);
    voucher.setElderName("对账测试");
    voucher.setVoucherNo("RF-TEST-" + System.nanoTime());
    voucher.setAmount(BigDecimal.TEN);
    voucher.setStatus("PAID");
    voucher.setPayMethod("OFFLINE_REFUND");
    voucher.setExecutedBy(500L);
    voucher.setExecutedByName("财务");
    voucher.setExecutedAt(LocalDateTime.now());
    financeRefundVoucherMapper.insert(voucher);

    var resp = financeService.reconcile(1L, LocalDate.now());
    assertEquals(0, resp.isMismatch() ? 1 : 0);
    assertEquals(0, BigDecimal.valueOf(30).compareTo(resp.getTotalReceived()));
    assertEquals(0, BigDecimal.TEN.compareTo(resp.getTotalRefund()));
    assertEquals(0, BigDecimal.valueOf(20).compareTo(resp.getNetReceived()));
  }

  @Test
  void reconcile_updates_existing_daily_record_instead_of_inserting_duplicate() {
    LocalDate today = LocalDate.now();
    ReconciliationDaily existing = new ReconciliationDaily();
    existing.setOrgId(1L);
    existing.setReconcileDate(today);
    existing.setTotalReceived(BigDecimal.TEN);
    existing.setMismatchFlag(0);
    reconciliationDailyMapper.insert(existing);

    var resp = financeService.reconcile(1L, today);
    assertEquals(today, resp.getDate());

    long count = reconciliationDailyMapper.selectCount(
        com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(ReconciliationDaily.class)
            .eq(ReconciliationDaily::getOrgId, 1L)
            .eq(ReconciliationDaily::getReconcileDate, today)
            .eq(ReconciliationDaily::getIsDeleted, 0));
    assertEquals(1L, count);
  }
}
