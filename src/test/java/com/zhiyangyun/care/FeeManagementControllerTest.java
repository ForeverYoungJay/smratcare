package com.zhiyangyun.care;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.audit.entity.AuditLog;
import com.zhiyangyun.care.audit.mapper.AuditLogMapper;
import com.zhiyangyun.care.auth.model.LoginRequest;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.finance.model.AdmissionFeeAuditCreateRequest;
import com.zhiyangyun.care.finance.model.DischargeSettlementConfirmRequest;
import com.zhiyangyun.care.finance.model.DischargeSettlementCreateRequest;
import com.zhiyangyun.care.finance.model.ElderAccountAdjustRequest;
import com.zhiyangyun.care.finance.service.ElderAccountService;
import com.zhiyangyun.care.finance.service.FeeManagementService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FeeManagementControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ElderMapper elderMapper;

  @Autowired
  private FeeManagementService feeManagementService;

  @Autowired
  private ElderAccountService elderAccountService;

  @Autowired
  private AuditLogMapper auditLogMapper;

  @Test
  void consumption_page_rejects_invalid_date_range() throws Exception {
    String token = loginAndGetToken("admin", "123456");
    mockMvc.perform(get("/api/finance/fee/consumption/page")
            .param("from", "2026-03-01")
            .param("to", "2026-02-01")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code", is(400)));
  }

  @Test
  void review_reject_requires_review_remark() throws Exception {
    Long elderId = createElder("控制器驳回校验");
    AdmissionFeeAuditCreateRequest request = new AdmissionFeeAuditCreateRequest();
    request.setElderId(elderId);
    request.setTotalAmount(BigDecimal.valueOf(123));
    request.setDepositAmount(BigDecimal.valueOf(22));
    var audit = feeManagementService.createAdmissionAudit(1L, 500L, request);

    String token = loginAndGetToken("admin", "123456");
    String body = """
        {
          "status": "REJECTED"
        }
        """;

    mockMvc.perform(put("/api/finance/fee/admission-audit/{id}/review", audit.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code", is(400)));
  }

  @Test
  void admission_page_supports_keyword_query() throws Exception {
    Long elderId = createElder("控制器关键字老人");
    AdmissionFeeAuditCreateRequest request = new AdmissionFeeAuditCreateRequest();
    request.setElderId(elderId);
    request.setTotalAmount(BigDecimal.valueOf(168));
    request.setDepositAmount(BigDecimal.valueOf(66));
    request.setRemark("关键字查询-控制器");
    feeManagementService.createAdmissionAudit(1L, 500L, request);

    String token = loginAndGetToken("admin", "123456");
    mockMvc.perform(get("/api/finance/fee/admission-audit/page")
            .param("keyword", "关键字查询-控制器")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is(0)))
        .andExpect(jsonPath("$.data.list[0].id", notNullValue()));
  }

  @Test
  void discharge_settlement_confirm_should_write_structured_audit_log() throws Exception {
    Long elderId = createElder("结构化审计结算");
    deposit(elderId, BigDecimal.valueOf(120));

    DischargeSettlementCreateRequest request = new DischargeSettlementCreateRequest();
    request.setElderId(elderId);
    request.setPayableAmount(BigDecimal.valueOf(40));
    var settlement = feeManagementService.createDischargeSettlement(1L, 500L, request);

    String token = loginAndGetToken("admin", "123456");
    confirmSettlement(token, settlement.getId(), "FRONTDESK_APPROVE", "前台", "前台签字");
    confirmSettlement(token, settlement.getId(), "NURSING_APPROVE", "护理部", "护理签字");
    confirmSettlement(token, settlement.getId(), "FINANCE_REFUND", "财务", "财务退款");

    AuditLog latest = auditLogMapper.selectOne(Wrappers.lambdaQuery(AuditLog.class)
        .eq(AuditLog::getActionType, "FIN_DISCHARGE_SETTLEMENT_CONFIRM")
        .eq(AuditLog::getEntityId, settlement.getId())
        .orderByDesc(AuditLog::getId)
        .last("LIMIT 1"));
    assertNotNull(latest);
    assertNotNull(latest.getBeforeSnapshot());
    assertNotNull(latest.getAfterSnapshot());
    assertNotNull(latest.getContextJson());
    assertTrue(latest.getContextJson().contains("FINANCE_REFUND"));
  }

  private Long createElder(String name) {
    ElderProfile elder = new ElderProfile();
    elder.setTenantId(1L);
    elder.setOrgId(1L);
    elder.setElderCode("ELD-C-" + System.nanoTime());
    elder.setElderQrCode("QR-C-" + System.nanoTime());
    elder.setFullName(name);
    elder.setAdmissionDate(LocalDate.now());
    elder.setStatus(1);
    elderMapper.insert(elder);
    return elder.getId();
  }

  private String loginAndGetToken(String username, String password) throws Exception {
    LoginRequest request = new LoginRequest();
    request.setUsername(username);
    request.setPassword(password);

    String response = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    return objectMapper.readTree(response).path("data").path("token").asText();
  }

  private void deposit(Long elderId, BigDecimal amount) {
    ElderAccountAdjustRequest adjust = new ElderAccountAdjustRequest();
    adjust.setElderId(elderId);
    adjust.setAmount(amount);
    adjust.setDirection("CREDIT");
    adjust.setFundType("DEPOSIT");
    adjust.setRemark("控制器测试押金充值");
    elderAccountService.adjust(1L, 500L, adjust);
  }

  private void confirmSettlement(String token, Long settlementId, String action, String signerName, String remark)
      throws Exception {
    DischargeSettlementConfirmRequest request = new DischargeSettlementConfirmRequest();
    request.setAction(action);
    request.setSignerName(signerName);
    request.setRemark(remark);
    mockMvc.perform(post("/api/finance/fee/discharge-settlement/{id}/confirm", settlementId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is(0)));
  }
}
