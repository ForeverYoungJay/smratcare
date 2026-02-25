package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.finance.entity.DischargeSettlement;
import com.zhiyangyun.care.finance.mapper.DischargeSettlementMapper;
import com.zhiyangyun.care.finance.model.AdmissionFeeAuditCreateRequest;
import com.zhiyangyun.care.finance.model.DischargeFeeAuditCreateRequest;
import com.zhiyangyun.care.finance.model.DischargeSettlementCreateRequest;
import com.zhiyangyun.care.finance.model.FeeAuditReviewRequest;
import com.zhiyangyun.care.finance.model.FeeWorkflowConstants;
import com.zhiyangyun.care.finance.model.ElderAccountAdjustRequest;
import com.zhiyangyun.care.finance.model.MonthlyAllocationCreateRequest;
import com.zhiyangyun.care.finance.service.ElderAccountService;
import com.zhiyangyun.care.finance.service.FeeManagementService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FeeManagementServiceTest {
  @Autowired
  private FeeManagementService feeManagementService;

  @Autowired
  private ElderAccountService elderAccountService;

  @Autowired
  private ElderMapper elderMapper;

  @Autowired
  private DischargeSettlementMapper dischargeSettlementMapper;

  @Test
  void confirm_settlement_rejects_when_balance_insufficient() {
    Long elderId = createElder("结算测试A");
    recharge(elderId, BigDecimal.valueOf(50));

    DischargeSettlementCreateRequest req = new DischargeSettlementCreateRequest();
    req.setElderId(elderId);
    req.setPayableAmount(BigDecimal.valueOf(100));
    DischargeSettlement settlement = feeManagementService.createDischargeSettlement(1L, 500L, req);

    assertThrows(IllegalStateException.class,
        () -> feeManagementService.confirmDischargeSettlement(1L, 500L, settlement.getId(), null));
  }

  @Test
  void confirm_settlement_is_idempotent_and_clears_balance() {
    Long elderId = createElder("结算测试B");
    recharge(elderId, BigDecimal.valueOf(200));

    DischargeSettlementCreateRequest req = new DischargeSettlementCreateRequest();
    req.setElderId(elderId);
    req.setPayableAmount(BigDecimal.valueOf(120));
    DischargeSettlement settlement = feeManagementService.createDischargeSettlement(1L, 500L, req);

    DischargeSettlement first = feeManagementService.confirmDischargeSettlement(1L, 500L, settlement.getId(), null);
    assertEquals(FeeWorkflowConstants.SETTLEMENT_SETTLED, first.getStatus());

    DischargeSettlement second = feeManagementService.confirmDischargeSettlement(1L, 500L, settlement.getId(), null);
    assertEquals(FeeWorkflowConstants.SETTLEMENT_SETTLED, second.getStatus());

    var account = elderAccountService.getOrCreate(1L, elderId, 500L);
    assertEquals(0, BigDecimal.ZERO.compareTo(account.getBalance()));
  }

  @Test
  void approved_discharge_audit_auto_creates_settlement() {
    Long elderId = createElder("结算测试C");

    DischargeFeeAuditCreateRequest req = new DischargeFeeAuditCreateRequest();
    req.setElderId(elderId);
    req.setPayableAmount(BigDecimal.valueOf(88));
    var audit = feeManagementService.createDischargeAudit(1L, 500L, req);

    FeeAuditReviewRequest review = new FeeAuditReviewRequest();
    review.setStatus("APPROVED");
    feeManagementService.reviewDischargeAudit(1L, 500L, audit.getId(), review);

    long count = dischargeSettlementMapper.selectCount(Wrappers.lambdaQuery(DischargeSettlement.class)
        .eq(DischargeSettlement::getOrgId, 1L)
        .eq(DischargeSettlement::getElderId, elderId)
        .eq(DischargeSettlement::getIsDeleted, 0));
    assertEquals(1L, count);
  }

  @Test
  void duplicate_pending_admission_audit_should_be_rejected() {
    Long elderId = createElder("入住审核重复校验");

    AdmissionFeeAuditCreateRequest req = new AdmissionFeeAuditCreateRequest();
    req.setElderId(elderId);
    req.setAdmissionId(1001L);
    req.setTotalAmount(BigDecimal.valueOf(200));
    req.setDepositAmount(BigDecimal.valueOf(50));
    feeManagementService.createAdmissionAudit(1L, 500L, req);

    IllegalStateException ex = assertThrows(IllegalStateException.class,
        () -> feeManagementService.createAdmissionAudit(1L, 500L, req));
    assertEquals("存在待审核的入住费用单，请先完成审核", ex.getMessage());
  }

  @Test
  void duplicate_pending_discharge_audit_should_be_rejected() {
    Long elderId = createElder("退住审核重复校验");

    DischargeFeeAuditCreateRequest req = new DischargeFeeAuditCreateRequest();
    req.setElderId(elderId);
    req.setPayableAmount(BigDecimal.valueOf(66));
    req.setRemark("  首次待审核  ");
    feeManagementService.createDischargeAudit(1L, 500L, req);

    IllegalStateException ex = assertThrows(IllegalStateException.class,
        () -> feeManagementService.createDischargeAudit(1L, 500L, req));
    assertEquals("存在待审核的退住费用单，请先完成审核", ex.getMessage());
  }

  @Test
  void consumption_page_should_validate_invalid_date_range() {
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> feeManagementService.consumptionPage(1L, 1L, 10L, null,
            "2026-03-01", "2026-02-01", null, null));
    assertEquals("开始日期不能晚于结束日期", ex.getMessage());
  }

  @Test
  void admission_keyword_query_and_text_normalization_should_work() {
    Long elderId = createElder("关键字老人A");

    AdmissionFeeAuditCreateRequest req = new AdmissionFeeAuditCreateRequest();
    req.setElderId(elderId);
    req.setTotalAmount(BigDecimal.valueOf(300));
    req.setDepositAmount(BigDecimal.valueOf(120));
    req.setRemark("  含关键字-测试备注  ");
    feeManagementService.createAdmissionAudit(1L, 500L, req);

    var page = feeManagementService.admissionAuditPage(1L, 1L, 20L, null, null, "关键字-测试");
    Assertions.assertTrue(page.getRecords().stream()
        .anyMatch(item -> item.getElderId().equals(elderId)));
  }

  @Test
  void monthly_allocation_should_reject_zero_target_when_amount_positive() {
    MonthlyAllocationCreateRequest req = new MonthlyAllocationCreateRequest();
    req.setAllocationMonth("2026-02");
    req.setAllocationName("公共费用");
    req.setTotalAmount(BigDecimal.valueOf(100));
    req.setTargetCount(0);
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> feeManagementService.createMonthlyAllocation(1L, 500L, req));
    assertEquals("totalAmount > 0 时 targetCount 不能为 0", ex.getMessage());
  }

  @Test
  void admission_audit_reject_requires_review_remark() {
    Long elderId = createElder("入住驳回备注校验");
    AdmissionFeeAuditCreateRequest req = new AdmissionFeeAuditCreateRequest();
    req.setElderId(elderId);
    req.setTotalAmount(BigDecimal.valueOf(188));
    req.setDepositAmount(BigDecimal.valueOf(20));
    var audit = feeManagementService.createAdmissionAudit(1L, 500L, req);

    FeeAuditReviewRequest review = new FeeAuditReviewRequest();
    review.setStatus("REJECTED");
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> feeManagementService.reviewAdmissionAudit(1L, 500L, audit.getId(), review));
    assertEquals("驳回时请填写审核备注", ex.getMessage());
  }

  @Test
  void discharge_audit_reject_requires_review_remark() {
    Long elderId = createElder("退住驳回备注校验");
    DischargeFeeAuditCreateRequest req = new DischargeFeeAuditCreateRequest();
    req.setElderId(elderId);
    req.setPayableAmount(BigDecimal.valueOf(99));
    var audit = feeManagementService.createDischargeAudit(1L, 500L, req);

    FeeAuditReviewRequest review = new FeeAuditReviewRequest();
    review.setStatus("REJECTED");
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> feeManagementService.reviewDischargeAudit(1L, 500L, audit.getId(), review));
    assertEquals("驳回时请填写审核备注", ex.getMessage());
  }

  @Test
  void admission_audit_cannot_be_reviewed_twice() {
    Long elderId = createElder("入住重复审核校验");
    AdmissionFeeAuditCreateRequest req = new AdmissionFeeAuditCreateRequest();
    req.setElderId(elderId);
    req.setTotalAmount(BigDecimal.valueOf(260));
    req.setDepositAmount(BigDecimal.valueOf(60));
    var audit = feeManagementService.createAdmissionAudit(1L, 500L, req);

    FeeAuditReviewRequest approve = new FeeAuditReviewRequest();
    approve.setStatus("APPROVED");
    feeManagementService.reviewAdmissionAudit(1L, 500L, audit.getId(), approve);

    IllegalStateException ex = assertThrows(IllegalStateException.class,
        () -> feeManagementService.reviewAdmissionAudit(1L, 500L, audit.getId(), approve));
    assertEquals("仅待审核状态可操作", ex.getMessage());
  }

  @Test
  void confirm_settlement_concurrently_should_not_double_deduct() throws Exception {
    Long elderId = createElder("并发结算校验");
    recharge(elderId, BigDecimal.valueOf(180));

    DischargeSettlementCreateRequest req = new DischargeSettlementCreateRequest();
    req.setElderId(elderId);
    req.setPayableAmount(BigDecimal.valueOf(120));
    DischargeSettlement settlement = feeManagementService.createDischargeSettlement(1L, 500L, req);

    ExecutorService executor = Executors.newFixedThreadPool(2);
    CountDownLatch latch = new CountDownLatch(1);
    List<Callable<String>> tasks = new ArrayList<>();
    tasks.add(() -> {
      latch.await();
      feeManagementService.confirmDischargeSettlement(1L, 500L, settlement.getId(), null);
      return "OK";
    });
    tasks.add(() -> {
      latch.await();
      feeManagementService.confirmDischargeSettlement(1L, 500L, settlement.getId(), null);
      return "OK";
    });
    List<Future<String>> futures;
    try {
      futures = tasks.stream().map(executor::submit).toList();
      latch.countDown();
      int success = 0;
      int failed = 0;
      for (Future<String> future : futures) {
        try {
          future.get();
          success++;
        } catch (Exception ex) {
          failed++;
        }
      }
      Assertions.assertTrue(success >= 1);
      Assertions.assertTrue(failed <= 1);
    } finally {
      executor.shutdownNow();
    }

    var account = elderAccountService.getOrCreate(1L, elderId, 500L);
    assertEquals(0, BigDecimal.ZERO.compareTo(account.getBalance()));
  }

  private Long createElder(String name) {
    ElderProfile elder = new ElderProfile();
    elder.setTenantId(1L);
    elder.setOrgId(1L);
    elder.setElderCode("ELD-" + System.nanoTime());
    elder.setElderQrCode("QR-" + System.nanoTime());
    elder.setFullName(name);
    elder.setAdmissionDate(LocalDate.now());
    elder.setStatus(1);
    elderMapper.insert(elder);
    return elder.getId();
  }

  private void recharge(Long elderId, BigDecimal amount) {
    ElderAccountAdjustRequest adjust = new ElderAccountAdjustRequest();
    adjust.setElderId(elderId);
    adjust.setAmount(amount);
    adjust.setDirection("CREDIT");
    adjust.setRemark("测试充值");
    elderAccountService.adjust(1L, 500L, adjust);
  }
}
