package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.finance.entity.FinanceConsumerVoucher;
import com.zhiyangyun.care.finance.entity.FinanceConsumerVoucherUsage;
import com.zhiyangyun.care.finance.entity.PaymentRecord;
import com.zhiyangyun.care.finance.mapper.FinanceConsumerVoucherMapper;
import com.zhiyangyun.care.finance.mapper.FinanceConsumerVoucherUsageMapper;
import com.zhiyangyun.care.finance.mapper.PaymentRecordMapper;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.finance.model.FinanceVoucherIssueRequest;
import com.zhiyangyun.care.finance.model.FinanceVoucherView;
import com.zhiyangyun.care.finance.model.PaymentRequest;
import com.zhiyangyun.care.finance.model.PaymentVoucherUse;
import com.zhiyangyun.care.finance.service.FinanceService;
import com.zhiyangyun.care.ltci.entity.LtciSettlement;
import com.zhiyangyun.care.ltci.mapper.LtciSettlementMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

/** 收款抵扣计算器：应收 − 长护险 − 折扣 − 消费券 = 实收现金。 */
@SpringBootTest
@ActiveProfiles("test")
class FinancePaymentDeductionTest {
  private static final Long ORG_ID = 1L;

  @Autowired
  private FinanceService financeService;

  @Autowired
  private BillMonthlyMapper billMonthlyMapper;

  @Autowired
  private PaymentRecordMapper paymentRecordMapper;

  @Autowired
  private FinanceConsumerVoucherMapper voucherMapper;

  @Autowired
  private FinanceConsumerVoucherUsageMapper voucherUsageMapper;

  @Autowired
  private LtciSettlementMapper ltciSettlementMapper;

  @Autowired
  private com.zhiyangyun.care.finance.service.FinanceVoucherService financeVoucherService;

  @Autowired
  private com.zhiyangyun.care.elder.mapper.ElderMapper elderMapper;

  @AfterEach
  void clearAuth() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void full_deduction_with_zero_cash_settles_bill() {
    BillMonthly bill = insertBill(3001L, "2026-03", 300);
    FinanceConsumerVoucher voucher = insertVoucher(3001L, 100, 100, null, null);

    PaymentRequest request = baseRequest(BigDecimal.ZERO);
    request.setLtciDeductAmount(BigDecimal.valueOf(150));
    request.setDiscountAmount(BigDecimal.valueOf(50));
    request.setDiscountReason("协议折扣");
    request.setVoucherUses(List.of(voucherUse(voucher.getId(), 100)));

    var response = financeService.pay(bill.getId(), request, 500L);

    assertEquals("PAID", response.getStatus());
    assertEquals(0, BigDecimal.valueOf(300).compareTo(response.getSettledAmount()));
    assertEquals(0, BigDecimal.ZERO.compareTo(response.getCashAmount()));

    BillMonthly latest = billMonthlyMapper.selectById(bill.getId());
    assertEquals(0, BigDecimal.valueOf(300).compareTo(latest.getPaidAmount()));
    assertEquals(0, BigDecimal.ZERO.compareTo(latest.getOutstandingAmount()));

    PaymentRecord record = latestRecord(bill.getId());
    assertEquals(0, BigDecimal.ZERO.compareTo(record.getAmount()));
    assertEquals(0, BigDecimal.valueOf(150).compareTo(record.getLtciDeductAmount()));
    assertEquals(0, BigDecimal.valueOf(50).compareTo(record.getDiscountAmount()));
    assertEquals(0, BigDecimal.valueOf(100).compareTo(record.getVoucherAmount()));
    assertEquals(0, BigDecimal.valueOf(300).compareTo(record.getSettledAmount()));

    FinanceConsumerVoucher usedVoucher = voucherMapper.selectById(voucher.getId());
    assertEquals(0, BigDecimal.ZERO.compareTo(usedVoucher.getBalanceAmount()));
    assertEquals("USED", usedVoucher.getStatus());
  }

  @Test
  void settle_total_over_outstanding_is_rejected() {
    BillMonthly bill = insertBill(3002L, "2026-03", 100);
    FinanceConsumerVoucher voucher = insertVoucher(3002L, 80, 80, null, null);

    PaymentRequest request = baseRequest(BigDecimal.valueOf(50));
    request.setVoucherUses(List.of(voucherUse(voucher.getId(), 80)));

    assertThrows(IllegalArgumentException.class, () -> financeService.pay(bill.getId(), request, 500L));
  }

  @Test
  void zero_cash_and_zero_deduction_is_rejected() {
    BillMonthly bill = insertBill(3003L, "2026-03", 100);
    PaymentRequest request = baseRequest(BigDecimal.ZERO);

    assertThrows(IllegalArgumentException.class, () -> financeService.pay(bill.getId(), request, 500L));
  }

  @Test
  void discount_without_reason_is_rejected() {
    BillMonthly bill = insertBill(3004L, "2026-03", 100);
    PaymentRequest request = baseRequest(BigDecimal.valueOf(10));
    request.setDiscountAmount(BigDecimal.valueOf(20));

    assertThrows(IllegalArgumentException.class, () -> financeService.pay(bill.getId(), request, 500L));
  }

  @Test
  void expired_voucher_is_rejected() {
    BillMonthly bill = insertBill(3005L, "2026-03", 100);
    FinanceConsumerVoucher voucher = insertVoucher(
        3005L, 50, 50, LocalDate.now().minusDays(30), LocalDate.now().minusDays(1));

    PaymentRequest request = baseRequest(BigDecimal.ZERO);
    request.setVoucherUses(List.of(voucherUse(voucher.getId(), 50)));

    assertThrows(IllegalStateException.class, () -> financeService.pay(bill.getId(), request, 500L));
  }

  @Test
  void voucher_over_balance_is_rejected() {
    BillMonthly bill = insertBill(3006L, "2026-03", 100);
    FinanceConsumerVoucher voucher = insertVoucher(3006L, 50, 20, null, null);

    PaymentRequest request = baseRequest(BigDecimal.ZERO);
    request.setVoucherUses(List.of(voucherUse(voucher.getId(), 30)));

    assertThrows(IllegalStateException.class, () -> financeService.pay(bill.getId(), request, 500L));
  }

  @Test
  void voucher_bound_to_another_elder_is_rejected() {
    BillMonthly bill = insertBill(3007L, "2026-03", 100);
    FinanceConsumerVoucher voucher = insertVoucher(9999L, 50, 50, null, null);

    PaymentRequest request = baseRequest(BigDecimal.ZERO);
    request.setVoucherUses(List.of(voucherUse(voucher.getId(), 50)));

    assertThrows(IllegalStateException.class, () -> financeService.pay(bill.getId(), request, 500L));
  }

  @Test
  void non_splittable_voucher_must_be_used_in_full() {
    BillMonthly bill = insertBill(3008L, "2026-03", 100);
    FinanceConsumerVoucher voucher = insertVoucher(3008L, 50, 50, null, null);
    voucher.setAllowSplit(0);
    voucherMapper.updateById(voucher);

    PaymentRequest request = baseRequest(BigDecimal.ZERO);
    request.setVoucherUses(List.of(voucherUse(voucher.getId(), 20)));

    assertThrows(IllegalStateException.class, () -> financeService.pay(bill.getId(), request, 500L));
  }

  @Test
  void voucher_below_min_bill_amount_is_rejected() {
    BillMonthly bill = insertBill(3009L, "2026-03", 100);
    FinanceConsumerVoucher voucher = insertVoucher(3009L, 50, 50, null, null);
    voucher.setMinBillAmount(BigDecimal.valueOf(500));
    voucherMapper.updateById(voucher);

    PaymentRequest request = baseRequest(BigDecimal.ZERO);
    request.setVoucherUses(List.of(voucherUse(voucher.getId(), 50)));

    assertThrows(IllegalStateException.class, () -> financeService.pay(bill.getId(), request, 500L));
  }

  @Test
  void ltci_deduct_over_fund_pay_quota_is_rejected() {
    BillMonthly bill = insertBill(3010L, "2026-03", 300);
    insertLtciSettlement(3010L, "202603", 5000L);

    PaymentRequest request = baseRequest(BigDecimal.ZERO);
    request.setLtciDeductAmount(BigDecimal.valueOf(60));

    assertThrows(IllegalArgumentException.class, () -> financeService.pay(bill.getId(), request, 500L));
  }

  @Test
  void ltci_deduct_within_fund_pay_quota_is_accepted() {
    BillMonthly bill = insertBill(3011L, "2026-03", 300);
    insertLtciSettlement(3011L, "202603", 5000L);

    PaymentRequest request = baseRequest(BigDecimal.valueOf(20));
    request.setLtciDeductAmount(BigDecimal.valueOf(50));

    var response = financeService.pay(bill.getId(), request, 500L);
    assertEquals("PARTIALLY_PAID", response.getStatus());
    assertEquals(0, BigDecimal.valueOf(70).compareTo(response.getSettledAmount()));
  }

  @Test
  void updating_payment_releases_and_reconsumes_voucher() {
    BillMonthly bill = insertBill(3012L, "2026-03", 200);
    FinanceConsumerVoucher voucher = insertVoucher(3012L, 100, 100, null, null);

    PaymentRequest create = baseRequest(BigDecimal.valueOf(20));
    create.setVoucherUses(List.of(voucherUse(voucher.getId(), 100)));
    financeService.pay(bill.getId(), create, 500L);

    FinanceConsumerVoucher afterPay = voucherMapper.selectById(voucher.getId());
    assertEquals(0, BigDecimal.ZERO.compareTo(afterPay.getBalanceAmount()));
    assertEquals("USED", afterPay.getStatus());

    PaymentRecord record = latestRecord(bill.getId());
    PaymentRequest update = baseRequest(BigDecimal.valueOf(20));
    update.setVoucherUses(List.of(voucherUse(voucher.getId(), 40)));
    financeService.updatePaymentRecord(record.getId(), update, 501L);

    FinanceConsumerVoucher afterUpdate = voucherMapper.selectById(voucher.getId());
    assertEquals(0, BigDecimal.valueOf(60).compareTo(afterUpdate.getBalanceAmount()));
    assertEquals("ACTIVE", afterUpdate.getStatus());

    BillMonthly latestBill = billMonthlyMapper.selectById(bill.getId());
    assertEquals(0, BigDecimal.valueOf(60).compareTo(latestBill.getPaidAmount()));
    assertEquals(0, BigDecimal.valueOf(140).compareTo(latestBill.getOutstandingAmount()));

    List<FinanceConsumerVoucherUsage> usages = voucherUsageMapper.selectList(
        Wrappers.lambdaQuery(FinanceConsumerVoucherUsage.class)
            .eq(FinanceConsumerVoucherUsage::getIsDeleted, 0)
            .eq(FinanceConsumerVoucherUsage::getVoucherId, voucher.getId()));
    assertTrue(usages.stream().anyMatch(item -> "RELEASE".equals(item.getDirection())));
  }

  @Test
  void deduction_preview_reports_outstanding_and_usable_vouchers() {
    BillMonthly bill = insertBill(3013L, "2026-03", 200);
    insertVoucher(3013L, 60, 60, null, null);
    insertLtciSettlement(3013L, "202603", 7500L);

    var preview = financeService.deductionPreview(bill.getId());

    assertEquals(0, BigDecimal.valueOf(200).compareTo(preview.getTotalAmount()));
    assertEquals(0, BigDecimal.valueOf(200).compareTo(preview.getOutstandingAmount()));
    assertEquals(0, BigDecimal.valueOf(75).compareTo(preview.getLtciFundPayAmount()));
    assertEquals(0, BigDecimal.valueOf(75).compareTo(preview.getLtciAvailableAmount()));
    assertTrue(preview.getUsableVouchers().stream()
        .anyMatch(item -> item.getElderId() != null && item.getElderId().equals(3013L)));
  }

  @Test
  void issue_generic_voucher_without_elder_binding() {
    // 发放通用券（elderIds 为空）曾因 List.of 不接受 null 元素直接 NPE，这里锁住该路径
    mockFinanceAuth(ORG_ID);
    FinanceVoucherIssueRequest request = new FinanceVoucherIssueRequest();
    request.setVoucherName("通用券回归");
    request.setFaceAmount(new BigDecimal("50"));
    request.setValidTo(LocalDate.now().plusMonths(1));

    List<FinanceVoucherView> created = financeVoucherService.issue(request, 500L);

    assertEquals(1, created.size());
    assertNull(created.get(0).getElderId(), "通用券不绑定长者");
    assertEquals(0, new BigDecimal("50.00").compareTo(created.get(0).getBalanceAmount()));
    assertTrue(financeVoucherService.listUsable(null, BigDecimal.ZERO, LocalDate.now()).stream()
        .anyMatch(item -> "通用券回归".equals(item.getVoucherName())));
  }

  @Test
  void issue_voucher_for_each_selected_elder() {
    mockFinanceAuth(ORG_ID);
    ElderProfile first = insertElder("发券对象甲");
    ElderProfile second = insertElder("发券对象乙");
    FinanceVoucherIssueRequest request = new FinanceVoucherIssueRequest();
    request.setVoucherName("批量发券回归");
    request.setFaceAmount(new BigDecimal("80"));
    request.setElderIds(List.of(first.getId(), second.getId()));

    List<FinanceVoucherView> created = financeVoucherService.issue(request, 500L);

    assertEquals(2, created.size(), "选中 N 位长者应各生成 1 张");
    assertEquals(2, created.stream().map(FinanceVoucherView::getVoucherNo).distinct().count(),
        "券号必须各自独立");
  }

  private ElderProfile insertElder(String fullName) {
    ElderProfile elder = new ElderProfile();
    elder.setTenantId(ORG_ID);
    elder.setOrgId(ORG_ID);
    elder.setFullName(fullName);
    elder.setStatus(1);
    elderMapper.insert(elder);
    return elder;
  }

  private void mockFinanceAuth(Long orgId) {
    var auth = new UsernamePasswordAuthenticationToken(
        String.valueOf(orgId), "N/A", List.of(new SimpleGrantedAuthority("ROLE_FINANCE_EMPLOYEE")));
    auth.setDetails(java.util.Map.of("orgId", orgId, "username", "voucher-tester"));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  private BillMonthly insertBill(Long elderId, String billMonth, int totalAmount) {
    BillMonthly bill = new BillMonthly();
    bill.setOrgId(ORG_ID);
    bill.setElderId(elderId);
    bill.setBillMonth(billMonth);
    bill.setTotalAmount(BigDecimal.valueOf(totalAmount));
    bill.setPaidAmount(BigDecimal.ZERO);
    bill.setOutstandingAmount(BigDecimal.valueOf(totalAmount));
    billMonthlyMapper.insert(bill);
    return bill;
  }

  private FinanceConsumerVoucher insertVoucher(
      Long elderId, int faceAmount, int balanceAmount, LocalDate validFrom, LocalDate validTo) {
    FinanceConsumerVoucher voucher = new FinanceConsumerVoucher();
    voucher.setOrgId(ORG_ID);
    voucher.setElderId(elderId);
    voucher.setVoucherNo("CV-TEST-" + elderId + "-" + faceAmount + "-" + balanceAmount);
    voucher.setVoucherName("测试券");
    voucher.setFaceAmount(BigDecimal.valueOf(faceAmount));
    voucher.setBalanceAmount(BigDecimal.valueOf(balanceAmount));
    voucher.setMinBillAmount(BigDecimal.ZERO);
    voucher.setAllowSplit(1);
    voucher.setValidFrom(validFrom);
    voucher.setValidTo(validTo);
    voucher.setStatus("ACTIVE");
    voucherMapper.insert(voucher);
    return voucher;
  }

  private void insertLtciSettlement(Long elderId, String settleMonth, long fundPayCents) {
    LtciSettlement settlement = new LtciSettlement();
    settlement.setOrgId(ORG_ID);
    settlement.setElderId(elderId);
    settlement.setSettleMonth(settleMonth);
    settlement.setServiceDays(30);
    settlement.setTotalFee(fundPayCents * 2);
    settlement.setFundPay(fundPayCents);
    settlement.setSelfPay(fundPayCents);
    settlement.setOverQuota(0L);
    settlement.setSettleStatus("SUBMITTED");
    settlement.setIsDeleted(0);
    ltciSettlementMapper.insert(settlement);
  }

  private PaymentRequest baseRequest(BigDecimal cash) {
    PaymentRequest request = new PaymentRequest();
    request.setAmount(cash);
    request.setMethod("CASH");
    request.setPaidAt(LocalDateTime.now());
    return request;
  }

  private PaymentVoucherUse voucherUse(Long voucherId, int amount) {
    PaymentVoucherUse use = new PaymentVoucherUse();
    use.setVoucherId(voucherId);
    use.setAmount(BigDecimal.valueOf(amount));
    return use;
  }

  private PaymentRecord latestRecord(Long billId) {
    return paymentRecordMapper.selectOne(
        Wrappers.lambdaQuery(PaymentRecord.class)
            .eq(PaymentRecord::getIsDeleted, 0)
            .eq(PaymentRecord::getBillMonthlyId, billId)
            .orderByDesc(PaymentRecord::getId)
            .last("LIMIT 1"));
  }
}
