package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.finance.model.DepositStandardRequest;
import com.zhiyangyun.care.finance.model.DepositTransactionRequest;
import com.zhiyangyun.care.finance.service.DepositService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

/** 押金：在押余额 = 已缴 − 已扣 − 已退；应缴差额 = 标准 − 已缴。 */
@SpringBootTest
@ActiveProfiles("test")
class DepositServiceTest {
  // 独立机构号，避免污染 org 1 的聚合断言；summary 这类整机构聚合的用例再单独换号，
  // 否则会被同一个类里其他用例建出来的押金账户带偏
  private static final Long DEFAULT_ORG_ID = 9401L;
  private Long orgId = DEFAULT_ORG_ID;

  @Autowired
  private DepositService depositService;

  @Autowired
  private ElderMapper elderMapper;

  @BeforeEach
  void mockAuth() {
    useOrg(DEFAULT_ORG_ID);
  }

  private void useOrg(Long targetOrgId) {
    orgId = targetOrgId;
    var auth = new UsernamePasswordAuthenticationToken(
        String.valueOf(targetOrgId), "N/A", List.of(new SimpleGrantedAuthority("ROLE_FINANCE_EMPLOYEE")));
    auth.setDetails(Map.of("orgId", targetOrgId, "username", "deposit-tester"));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void clearAuth() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void standard_resolves_by_care_level_then_falls_back_to_default() {
    saveStandard("通用押金", null, "3000");
    saveStandard("三级护理押金", "三级护理", "5000");

    assertEquals(0, new BigDecimal("5000.00").compareTo(depositService.resolveStandardAmount("三级护理")));
    assertEquals(0, new BigDecimal("3000.00").compareTo(depositService.resolveStandardAmount("一级护理")));
    assertEquals(0, new BigDecimal("3000.00").compareTo(depositService.resolveStandardAmount(null)));
  }

  @Test
  void pay_accumulates_and_status_moves_from_unpaid_to_paid() {
    saveStandard("通用押金", null, "1000");
    ElderProfile elder = insertElder("押金测试甲", null);

    var partial = depositService.registerPay(txn(elder.getId(), "400", null), 800L);
    assertEquals("PARTIAL", partial.getStatus());
    assertEquals(0, new BigDecimal("600.00").compareTo(partial.getShortfallAmount()));
    assertEquals(0, new BigDecimal("400.00").compareTo(partial.getBalanceAmount()));

    var full = depositService.registerPay(txn(elder.getId(), "600", null), 800L);
    assertEquals("PAID", full.getStatus());
    assertEquals(0, BigDecimal.ZERO.compareTo(full.getShortfallAmount()));
    assertEquals(0, new BigDecimal("1000.00").compareTo(full.getBalanceAmount()));
  }

  @Test
  void deduct_and_refund_reduce_balance_and_close_account() {
    saveStandard("通用押金", null, "1000");
    ElderProfile elder = insertElder("押金测试乙", null);
    depositService.registerPay(txn(elder.getId(), "1000", null), 800L);

    var afterDeduct = depositService.registerDeduct(txn(elder.getId(), "300", "损坏赔偿"), 800L);
    assertEquals(0, new BigDecimal("700.00").compareTo(afterDeduct.getBalanceAmount()));
    assertEquals(0, new BigDecimal("300.00").compareTo(afterDeduct.getDeductedAmount()));

    var afterRefund = depositService.registerRefund(txn(elder.getId(), "700", "退住结算退还"), 800L);
    assertEquals(0, BigDecimal.ZERO.compareTo(afterRefund.getBalanceAmount()));
    assertEquals("CLOSED", afterRefund.getStatus());

    assertEquals(3, depositService.transactions(elder.getId()).size());
  }

  @Test
  void deduct_over_balance_is_rejected() {
    saveStandard("通用押金", null, "1000");
    ElderProfile elder = insertElder("押金测试丙", null);
    depositService.registerPay(txn(elder.getId(), "200", null), 800L);

    DepositTransactionRequest request = txn(elder.getId(), "500", "超额扣款");
    assertThrows(IllegalStateException.class, () -> depositService.registerDeduct(request, 800L));
  }

  @Test
  void refund_over_balance_is_rejected() {
    saveStandard("通用押金", null, "1000");
    ElderProfile elder = insertElder("押金测试丁", null);
    depositService.registerPay(txn(elder.getId(), "200", null), 800L);

    DepositTransactionRequest request = txn(elder.getId(), "300", "超额退还");
    assertThrows(IllegalStateException.class, () -> depositService.registerRefund(request, 800L));
  }

  @Test
  void deduct_without_reason_is_rejected() {
    saveStandard("通用押金", null, "1000");
    ElderProfile elder = insertElder("押金测试戊", null);
    depositService.registerPay(txn(elder.getId(), "500", null), 800L);

    DepositTransactionRequest request = txn(elder.getId(), "100", null);
    assertThrows(IllegalArgumentException.class, () -> depositService.registerDeduct(request, 800L));
  }

  @Test
  void refresh_standard_picks_up_care_level_change() {
    saveStandard("通用押金", null, "1000");
    saveStandard("特级护理押金", "特级护理", "8000");
    ElderProfile elder = insertElder("押金测试己", null);
    depositService.registerPay(txn(elder.getId(), "100", null), 800L);

    elder.setCareLevel("特级护理");
    elderMapper.updateById(elder);
    var refreshed = depositService.refreshStandard(elder.getId());

    assertEquals(0, new BigDecimal("8000.00").compareTo(refreshed.getStandardAmount()));
    assertEquals(0, new BigDecimal("7900.00").compareTo(refreshed.getShortfallAmount()));
  }

  @Test
  void summary_counts_shortfall_accounts() {
    useOrg(9402L);
    saveStandard("通用押金", null, "1000");
    ElderProfile paid = insertElder("押金汇总甲", null);
    ElderProfile owing = insertElder("押金汇总乙", null);
    depositService.registerPay(txn(paid.getId(), "1000", null), 800L);
    depositService.registerPay(txn(owing.getId(), "250", null), 800L);

    var summary = depositService.summary();
    assertEquals(2, summary.getAccountCount());
    assertEquals(1, summary.getShortfallCount());
    assertEquals(0, new BigDecimal("750.00").compareTo(summary.getShortfallAmount()));
    assertEquals(0, new BigDecimal("1250.00").compareTo(summary.getTotalBalance()));
  }

  private void saveStandard(String name, String careLevel, String amount) {
    DepositStandardRequest request = new DepositStandardRequest();
    request.setStandardName(name);
    request.setCareLevel(careLevel);
    request.setAmount(new BigDecimal(amount));
    request.setEffectiveFrom(LocalDate.now().minusDays(1));
    depositService.saveStandard(request, 800L);
  }

  private ElderProfile insertElder(String fullName, String careLevel) {
    ElderProfile elder = new ElderProfile();
    elder.setTenantId(orgId);
    elder.setOrgId(orgId);
    elder.setFullName(fullName);
    elder.setCareLevel(careLevel);
    elder.setStatus(1);
    elderMapper.insert(elder);
    return elder;
  }

  private DepositTransactionRequest txn(Long elderId, String amount, String reason) {
    DepositTransactionRequest request = new DepositTransactionRequest();
    request.setElderId(elderId);
    request.setAmount(new BigDecimal(amount));
    request.setReason(reason);
    return request;
  }
}
