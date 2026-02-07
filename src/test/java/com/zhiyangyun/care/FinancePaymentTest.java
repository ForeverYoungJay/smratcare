package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.finance.model.PaymentRequest;
import com.zhiyangyun.care.finance.service.FinanceService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FinancePaymentTest {
  @Autowired
  private FinanceService financeService;

  @Autowired
  private BillMonthlyMapper billMonthlyMapper;

  @Test
  void partial_payment_updates_status() {
    BillMonthly bill = new BillMonthly();
    bill.setOrgId(1L);
    bill.setElderId(200L);
    bill.setBillMonth("2026-01");
    bill.setTotalAmount(BigDecimal.valueOf(100));
    bill.setPaidAmount(BigDecimal.ZERO);
    bill.setOutstandingAmount(BigDecimal.valueOf(100));
    billMonthlyMapper.insert(bill);

    PaymentRequest request = new PaymentRequest();
    request.setAmount(BigDecimal.valueOf(40));
    request.setMethod("CASH");
    request.setPaidAt(LocalDateTime.now());

    var response = financeService.pay(bill.getId(), request, 500L);
    assertEquals("PARTIALLY_PAID", response.getStatus());
  }

  @Test
  void overpay_rejected() {
    BillMonthly bill = new BillMonthly();
    bill.setOrgId(1L);
    bill.setElderId(201L);
    bill.setBillMonth("2026-01");
    bill.setTotalAmount(BigDecimal.valueOf(50));
    bill.setPaidAmount(BigDecimal.ZERO);
    bill.setOutstandingAmount(BigDecimal.valueOf(50));
    billMonthlyMapper.insert(bill);

    PaymentRequest request = new PaymentRequest();
    request.setAmount(BigDecimal.valueOf(60));
    request.setMethod("CASH");
    request.setPaidAt(LocalDateTime.now());

    assertThrows(IllegalArgumentException.class, () -> financeService.pay(bill.getId(), request, 500L));
  }

  @Test
  void status_paid_when_full() {
    BillMonthly bill = new BillMonthly();
    bill.setOrgId(1L);
    bill.setElderId(202L);
    bill.setBillMonth("2026-01");
    bill.setTotalAmount(BigDecimal.valueOf(80));
    bill.setPaidAmount(BigDecimal.ZERO);
    bill.setOutstandingAmount(BigDecimal.valueOf(80));
    billMonthlyMapper.insert(bill);

    PaymentRequest request = new PaymentRequest();
    request.setAmount(BigDecimal.valueOf(80));
    request.setMethod("BANK");
    request.setPaidAt(LocalDateTime.now());

    var response = financeService.pay(bill.getId(), request, 500L);
    assertEquals("PAID", response.getStatus());
  }
}
