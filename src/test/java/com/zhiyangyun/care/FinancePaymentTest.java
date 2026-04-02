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

  @Test
  void duplicate_external_txn_id_is_idempotent_for_same_bill() {
    BillMonthly bill = new BillMonthly();
    bill.setOrgId(1L);
    bill.setElderId(203L);
    bill.setBillMonth("2026-01");
    bill.setTotalAmount(BigDecimal.valueOf(120));
    bill.setPaidAmount(BigDecimal.ZERO);
    bill.setOutstandingAmount(BigDecimal.valueOf(120));
    billMonthlyMapper.insert(bill);

    PaymentRequest request = new PaymentRequest();
    request.setAmount(BigDecimal.valueOf(30));
    request.setMethod("cash");
    request.setPaidAt(LocalDateTime.now());
    request.setExternalTxnId("TXN-1001");

    financeService.pay(bill.getId(), request, 500L);
    var response = financeService.pay(bill.getId(), request, 500L);
    BillMonthly latest = billMonthlyMapper.selectById(bill.getId());

    assertEquals("PARTIALLY_PAID", response.getStatus());
    assertEquals(0, BigDecimal.valueOf(30).compareTo(latest.getPaidAmount()));
    assertEquals(0, BigDecimal.valueOf(90).compareTo(latest.getOutstandingAmount()));
  }

  @Test
  void duplicate_external_txn_id_for_another_bill_should_be_rejected() {
    BillMonthly first = new BillMonthly();
    first.setOrgId(1L);
    first.setElderId(204L);
    first.setBillMonth("2026-01");
    first.setTotalAmount(BigDecimal.valueOf(100));
    first.setPaidAmount(BigDecimal.ZERO);
    first.setOutstandingAmount(BigDecimal.valueOf(100));
    billMonthlyMapper.insert(first);

    BillMonthly second = new BillMonthly();
    second.setOrgId(1L);
    second.setElderId(205L);
    second.setBillMonth("2026-01");
    second.setTotalAmount(BigDecimal.valueOf(100));
    second.setPaidAmount(BigDecimal.ZERO);
    second.setOutstandingAmount(BigDecimal.valueOf(100));
    billMonthlyMapper.insert(second);

    PaymentRequest request = new PaymentRequest();
    request.setAmount(BigDecimal.valueOf(20));
    request.setMethod("CASH");
    request.setPaidAt(LocalDateTime.now());
    request.setExternalTxnId("TXN-1002");

    financeService.pay(first.getId(), request, 500L);
    assertThrows(IllegalStateException.class, () -> financeService.pay(second.getId(), request, 500L));
  }

  @Test
  void invalidate_bill_with_payment_should_be_rejected() {
    BillMonthly bill = new BillMonthly();
    bill.setOrgId(1L);
    bill.setElderId(206L);
    bill.setBillMonth("2026-01");
    bill.setTotalAmount(BigDecimal.valueOf(70));
    bill.setPaidAmount(BigDecimal.ZERO);
    bill.setOutstandingAmount(BigDecimal.valueOf(70));
    billMonthlyMapper.insert(bill);

    PaymentRequest request = new PaymentRequest();
    request.setAmount(BigDecimal.valueOf(20));
    request.setMethod("CASH");
    request.setPaidAt(LocalDateTime.now());
    financeService.pay(bill.getId(), request, 500L);

    assertThrows(IllegalStateException.class, () -> financeService.invalidateBill(bill.getId(), 500L));
  }
}
