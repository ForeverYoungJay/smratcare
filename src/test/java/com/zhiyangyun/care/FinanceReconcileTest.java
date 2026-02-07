package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
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

    var resp = financeService.reconcile(1L, LocalDate.now());
    assertEquals(0, resp.isMismatch() ? 1 : 0);
  }
}
