package com.zhiyangyun.care;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.model.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OaPortalControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @Sql(statements = {
      "DELETE FROM health_inspection WHERE id = 8903",
      "DELETE FROM health_medication_task WHERE id = 8902",
      "DELETE FROM crm_contract WHERE id = 8901 OR contract_no = 'UT-PORTAL-8901'",
      "INSERT INTO crm_contract (id, tenant_id, org_id, elder_id, contract_no, status, effective_to, is_deleted) "
          + "VALUES (8901, 1, 1, 990001, 'UT-PORTAL-8901', 'SIGNED', DATEADD('DAY', 10, CURRENT_DATE), 0)",
      "INSERT INTO health_medication_task (id, tenant_id, org_id, setting_id, elder_id, elder_name, drug_name, planned_time, task_date, status, is_deleted) "
          + "VALUES (8902, 1, 1, 990002, 990001, '首页测试长者', '测试药品', CURRENT_TIMESTAMP, CURRENT_DATE, 'PENDING', 0)",
      "INSERT INTO health_inspection (id, tenant_id, org_id, elder_id, elder_name, inspection_date, inspection_item, status, is_deleted) "
          + "VALUES (8903, 1, 1, 990001, '首页测试长者', CURRENT_DATE, '血压', 'ABNORMAL', 0)"
  })
  void summary_uses_elder_contract_and_medical_counts() throws Exception {
    String token = loginAndGetToken("admin", "123456");

    mockMvc.perform(get("/api/oa/portal/summary")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is(0)))
        .andExpect(jsonPath("$.data.pendingMedicalOrderCount", greaterThanOrEqualTo(1)))
        .andExpect(jsonPath("$.data.elderContractExpiringCount", greaterThanOrEqualTo(1)))
        .andExpect(jsonPath("$.data.healthAbnormalCount", greaterThanOrEqualTo(1)))
        .andExpect(jsonPath("$.data.elderAbnormalCount", is(0)));
  }

  private String loginAndGetToken(String username, String password) throws Exception {
    LoginRequest request = new LoginRequest();
    request.setUsername(username);
    request.setPassword(password);

    String response = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    JsonNode node = objectMapper.readTree(response);
    return node.path("data").path("token").asText();
  }
}
