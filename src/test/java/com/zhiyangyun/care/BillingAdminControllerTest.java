package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.bill.controller.BillingAdminController;
import com.zhiyangyun.care.bill.entity.BillingCareLevelFee;
import com.zhiyangyun.care.bill.model.BillingCareLevelFeeRequest;
import com.zhiyangyun.care.bill.service.BillingConfigService;
import jakarta.validation.Validation;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

class BillingAdminControllerTest {
  private final BillingConfigService service = mock(BillingConfigService.class);
  private final AuditLogService audit = mock(AuditLogService.class);
  private final BillingAdminController controller = new BillingAdminController(service, audit);

  @BeforeEach
  void authenticate() {
    var auth = new UsernamePasswordAuthenticationToken("7", null, AuthorityUtils.createAuthorityList("ROLE_FINANCE_MINISTER"));
    auth.setDetails(Map.of("orgId", 1L, "username", "财务主管"));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void clearAuthentication() { SecurityContextHolder.clearContext(); }

  private BillingCareLevelFeeRequest request() {
    var request = new BillingCareLevelFeeRequest();
    request.setOrgId(1L);
    request.setCareLevel("L2");
    request.setFeeMonthly(BigDecimal.ZERO);
    request.setEffectiveMonth("2026-11");
    request.setStatus(0);
    return request;
  }

  @Test
  void savesZeroAndDisabledStatusWithAnAuditRecord() {
    when(service.upsertCareLevelFee(any())).thenAnswer(invocation -> {
      BillingCareLevelFee fee = invocation.getArgument(0);
      fee.setId(9L);
      return fee;
    });
    var saved = controller.upsertCareLevel(request());
    assertEquals(BigDecimal.ZERO, saved.getFeeMonthly());
    assertEquals(0, saved.getStatus());
    verify(audit).record(eq(1L), eq(1L), eq(7L), eq("财务主管"), eq("FIN_CARE_LEVEL_FEE_SAVE"),
        eq("FINANCE_CONFIG"), eq(9L), contains("L2@2026-11"));
  }

  @Test
  void rejectsCrossOrganizationBeforeSaving() {
    var request = request();
    request.setOrgId(2L);
    assertThrows(AccessDeniedException.class, () -> controller.upsertCareLevel(request));
    verifyNoInteractions(service, audit);
  }

  @Test
  void validatesMoneyAndStatusAtTheRequestBoundary() {
    try (var factory = Validation.buildDefaultValidatorFactory()) {
      var request = request();
      assertTrue(factory.getValidator().validate(request).isEmpty());
      request.setFeeMonthly(new BigDecimal("-0.01"));
      request.setStatus(2);
      assertEquals(2, factory.getValidator().validate(request).size());
    }
  }
}
