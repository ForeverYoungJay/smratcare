package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.elder.controller.adm.ElderResidenceController;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderDischargeApply;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderDischargeApplyMapper;
import com.zhiyangyun.care.elder.model.lifecycle.DischargeApplyCreateRequest;
import com.zhiyangyun.care.elder.model.lifecycle.DischargeApplyReviewRequest;
import com.zhiyangyun.care.elder.model.lifecycle.ResidenceLifecycleConstants;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
class ElderResidenceFlowTest {
  @Autowired
  private ElderResidenceController residenceController;

  @Autowired
  private ElderMapper elderMapper;

  @Autowired
  private ElderDischargeApplyMapper applyMapper;

  @Autowired
  private BillMonthlyMapper billMonthlyMapper;

  @AfterEach
  void cleanupAuth() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void approve_apply_auto_discharge_success() {
    mockAdminAuth();
    ElderProfile elder = createElder("AutoSuccess");

    DischargeApplyCreateRequest createRequest = new DischargeApplyCreateRequest();
    createRequest.setElderId(elder.getId());
    createRequest.setPlannedDischargeDate(LocalDate.now());
    createRequest.setReason("家属申请");
    var created = residenceController.createDischargeApply(createRequest).getData();
    assertNotNull(created);

    DischargeApplyReviewRequest review = new DischargeApplyReviewRequest();
    review.setStatus(ResidenceLifecycleConstants.DISCHARGE_APPLY_APPROVED);
    review.setReviewRemark("同意");
    ElderDischargeApply reviewed = residenceController.reviewDischargeApply(created.getId(), review).getData();

    assertEquals(ResidenceLifecycleConstants.DISCHARGE_APPLY_APPROVED, reviewed.getStatus());
    assertEquals(ResidenceLifecycleConstants.AUTO_DISCHARGE_SUCCESS, reviewed.getAutoDischargeStatus());
    assertNotNull(reviewed.getLinkedDischargeId());
  }

  @Test
  void approve_apply_with_unpaid_bill_stays_pending_and_records_reason() {
    mockAdminAuth();
    ElderProfile elder = createElder("AutoFail");
    createUnpaidBill(elder.getId());

    DischargeApplyCreateRequest createRequest = new DischargeApplyCreateRequest();
    createRequest.setElderId(elder.getId());
    createRequest.setPlannedDischargeDate(LocalDate.now());
    createRequest.setReason("账单测试");
    ElderDischargeApply created = residenceController.createDischargeApply(createRequest).getData();
    assertNotNull(created);

    DischargeApplyReviewRequest review = new DischargeApplyReviewRequest();
    review.setStatus(ResidenceLifecycleConstants.DISCHARGE_APPLY_APPROVED);
    review.setReviewRemark("尝试自动退住");
    assertThrows(Exception.class, () -> residenceController.reviewDischargeApply(created.getId(), review));

    ElderDischargeApply latest = applyMapper.selectById(created.getId());
    assertEquals(ResidenceLifecycleConstants.DISCHARGE_APPLY_PENDING, latest.getStatus());
    assertEquals(ResidenceLifecycleConstants.AUTO_DISCHARGE_FAILED, latest.getAutoDischargeStatus());
    assertTrue(latest.getAutoDischargeMessage() != null && !latest.getAutoDischargeMessage().isBlank());
  }

  @Test
  void reviewed_apply_cannot_be_reviewed_twice() {
    mockAdminAuth();
    ElderProfile elder = createElder("ReviewOnce");

    DischargeApplyCreateRequest createRequest = new DischargeApplyCreateRequest();
    createRequest.setElderId(elder.getId());
    createRequest.setPlannedDischargeDate(LocalDate.now());
    createRequest.setReason("重复审核测试");
    ElderDischargeApply created = residenceController.createDischargeApply(createRequest).getData();
    assertNotNull(created);

    DischargeApplyReviewRequest reject = new DischargeApplyReviewRequest();
    reject.setStatus(ResidenceLifecycleConstants.DISCHARGE_APPLY_REJECTED);
    reject.setReviewRemark("资料不全");
    residenceController.reviewDischargeApply(created.getId(), reject);

    IllegalStateException ex = assertThrows(IllegalStateException.class,
        () -> residenceController.reviewDischargeApply(created.getId(), reject));
    assertEquals("该申请已审核，不能重复操作", ex.getMessage());
  }

  private void mockAdminAuth() {
    var auth = new UsernamePasswordAuthenticationToken(
        "9001",
        "N/A",
        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    auth.setDetails(Map.of("orgId", 1L, "username", "test-admin"));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  private ElderProfile createElder(String namePrefix) {
    ElderProfile elder = new ElderProfile();
    elder.setTenantId(1L);
    elder.setOrgId(1L);
    elder.setFullName(namePrefix + "-" + UUID.randomUUID().toString().substring(0, 6));
    elder.setGender(1);
    elder.setStatus(1);
    elder.setIsDeleted(0);
    elderMapper.insert(elder);
    return elder;
  }

  private void createUnpaidBill(Long elderId) {
    BillMonthly bill = new BillMonthly();
    bill.setOrgId(1L);
    bill.setElderId(elderId);
    bill.setBillMonth("2026-02");
    bill.setTotalAmount(BigDecimal.valueOf(100));
    bill.setPaidAmount(BigDecimal.ZERO);
    bill.setOutstandingAmount(BigDecimal.valueOf(100));
    bill.setIsDeleted(0);
    billMonthlyMapper.insert(bill);
  }
}
