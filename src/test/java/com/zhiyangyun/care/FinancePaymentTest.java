package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.finance.entity.PaymentRecord;
import com.zhiyangyun.care.finance.mapper.PaymentRecordMapper;
import com.zhiyangyun.care.finance.model.PaymentRequest;
import com.zhiyangyun.care.finance.service.FinanceService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FinancePaymentTest {
  @Autowired
  private FinanceService financeService;

  @Autowired
  private BillMonthlyMapper billMonthlyMapper;

  @Autowired
  private PaymentRecordMapper paymentRecordMapper;

  @AfterEach
  void clearAuth() {
    SecurityContextHolder.clearContext();
  }

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

  @Test
  void update_payment_record_should_update_external_txn_id() {
    BillMonthly bill = new BillMonthly();
    bill.setOrgId(1L);
    bill.setElderId(207L);
    bill.setBillMonth("2026-01");
    bill.setTotalAmount(BigDecimal.valueOf(100));
    bill.setPaidAmount(BigDecimal.ZERO);
    bill.setOutstandingAmount(BigDecimal.valueOf(100));
    billMonthlyMapper.insert(bill);

    PaymentRequest create = new PaymentRequest();
    create.setAmount(BigDecimal.valueOf(30));
    create.setMethod("CASH");
    create.setPaidAt(LocalDateTime.now().minusMinutes(2));
    financeService.pay(bill.getId(), create, 500L);

    PaymentRecord record = paymentRecordMapper.selectOne(
        com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(PaymentRecord.class)
            .eq(PaymentRecord::getBillMonthlyId, bill.getId())
            .eq(PaymentRecord::getIsDeleted, 0)
            .last("LIMIT 1"));

    PaymentRequest update = new PaymentRequest();
    update.setAmount(BigDecimal.valueOf(30));
    update.setMethod("BANK");
    update.setPaidAt(LocalDateTime.now());
    update.setExternalTxnId("TXN-UPD-2001");
    update.setRemark("change method");

    financeService.updatePaymentRecord(record.getId(), update, 501L);

    PaymentRecord latest = paymentRecordMapper.selectById(record.getId());
    assertEquals("TXN-UPD-2001", latest.getExternalTxnId());
    assertEquals("BANK", latest.getPayMethod());
  }

  @Test
  void update_payment_record_rejects_duplicate_external_txn_id() {
    BillMonthly first = new BillMonthly();
    first.setOrgId(1L);
    first.setElderId(208L);
    first.setBillMonth("2026-01");
    first.setTotalAmount(BigDecimal.valueOf(100));
    first.setPaidAmount(BigDecimal.ZERO);
    first.setOutstandingAmount(BigDecimal.valueOf(100));
    billMonthlyMapper.insert(first);

    BillMonthly second = new BillMonthly();
    second.setOrgId(1L);
    second.setElderId(209L);
    second.setBillMonth("2026-01");
    second.setTotalAmount(BigDecimal.valueOf(100));
    second.setPaidAmount(BigDecimal.ZERO);
    second.setOutstandingAmount(BigDecimal.valueOf(100));
    billMonthlyMapper.insert(second);

    PaymentRequest firstPay = new PaymentRequest();
    firstPay.setAmount(BigDecimal.valueOf(20));
    firstPay.setMethod("CASH");
    firstPay.setPaidAt(LocalDateTime.now().minusMinutes(3));
    firstPay.setExternalTxnId("TXN-UPD-2002");
    financeService.pay(first.getId(), firstPay, 500L);

    PaymentRequest secondPay = new PaymentRequest();
    secondPay.setAmount(BigDecimal.valueOf(20));
    secondPay.setMethod("CASH");
    secondPay.setPaidAt(LocalDateTime.now().minusMinutes(2));
    financeService.pay(second.getId(), secondPay, 500L);

    PaymentRecord secondRecord = paymentRecordMapper.selectOne(
        com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(PaymentRecord.class)
            .eq(PaymentRecord::getBillMonthlyId, second.getId())
            .eq(PaymentRecord::getIsDeleted, 0)
            .last("LIMIT 1"));

    PaymentRequest update = new PaymentRequest();
    update.setAmount(BigDecimal.valueOf(20));
    update.setMethod("BANK");
    update.setPaidAt(LocalDateTime.now());
    update.setExternalTxnId("TXN-UPD-2002");

    assertThrows(IllegalStateException.class,
        () -> financeService.updatePaymentRecord(secondRecord.getId(), update, 501L));
  }

  @Test
  void non_admin_cannot_pay_bill_of_another_org() {
    mockFinanceAuth(1L);
    BillMonthly bill = new BillMonthly();
    bill.setOrgId(2L);
    bill.setElderId(210L);
    bill.setBillMonth("2026-01");
    bill.setTotalAmount(BigDecimal.valueOf(100));
    bill.setPaidAmount(BigDecimal.ZERO);
    bill.setOutstandingAmount(BigDecimal.valueOf(100));
    billMonthlyMapper.insert(bill);

    PaymentRequest request = new PaymentRequest();
    request.setAmount(BigDecimal.valueOf(10));
    request.setMethod("CASH");
    request.setPaidAt(LocalDateTime.now());

    assertThrows(org.springframework.security.access.AccessDeniedException.class,
        () -> financeService.pay(bill.getId(), request, 500L));
  }

  private void mockFinanceAuth(Long orgId) {
    var auth = new UsernamePasswordAuthenticationToken(
        "9002",
        "N/A",
        List.of(new SimpleGrantedAuthority("ROLE_FINANCE_EMPLOYEE")));
    auth.setDetails(Map.of("orgId", orgId, "username", "finance-user"));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }
}
