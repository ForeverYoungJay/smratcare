package com.zhiyangyun.care;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.model.LoginRequest;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.finance.model.AdmissionFeeAuditCreateRequest;
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
}

