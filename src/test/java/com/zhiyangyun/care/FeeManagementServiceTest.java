package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.finance.entity.DischargeSettlement;
import com.zhiyangyun.care.finance.mapper.DischargeSettlementMapper;
import com.zhiyangyun.care.finance.model.DischargeFeeAuditCreateRequest;
import com.zhiyangyun.care.finance.model.DischargeSettlementCreateRequest;
import com.zhiyangyun.care.finance.model.FeeAuditReviewRequest;
import com.zhiyangyun.care.finance.model.FeeWorkflowConstants;
import com.zhiyangyun.care.finance.model.ElderAccountAdjustRequest;
import com.zhiyangyun.care.finance.service.ElderAccountService;
import com.zhiyangyun.care.finance.service.FeeManagementService;
import java.math.BigDecimal;
import java.time.LocalDate;
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
