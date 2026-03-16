package com.zhiyangyun.care;

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
class DashboardControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @Sql(statements = {
      "INSERT INTO bill_monthly (id, org_id, elder_id, bill_month, total_amount, paid_amount, outstanding_amount, status, is_deleted) "
          + "VALUES (8301, 1, 200, '2026-01', 100.00, 100.00, 0.00, 2, 0)",
      "INSERT INTO bill_monthly (id, org_id, elder_id, bill_month, total_amount, paid_amount, outstanding_amount, status, is_deleted) "
          + "VALUES (8302, 1, 200, '2026-02', 240.00, 240.00, 0.00, 2, 0)"
  })
  void summary_respects_requested_window() throws Exception {
    String token = loginAndGetToken("admin", "123456");

    mockMvc.perform(get("/api/dashboard/summary")
            .param("from", "2026-01")
            .param("to", "2026-01")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is(0)))
        .andExpect(jsonPath("$.data.statsFromMonth", is("2026-01")))
        .andExpect(jsonPath("$.data.statsToMonth", is("2026-01")))
        .andExpect(jsonPath("$.data.totalRevenue", is(100.00)))
        .andExpect(jsonPath("$.data.averageMonthlyRevenue", is(100.00)));
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
