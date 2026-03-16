package com.zhiyangyun.care;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
class StatisticsControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void checkIn_rejects_invalid_month_format() throws Exception {
    String token = loginAndGetToken("admin", "123456");

    mockMvc.perform(get("/api/stats/check-in")
            .param("from", "2026-13")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code", is(400)));
  }

  @Test
  void staff_cannot_query_other_org_stats() throws Exception {
    String token = loginAndGetToken("staff", "123456");

    mockMvc.perform(get("/api/stats/check-in")
            .param("from", "2026-01")
            .param("to", "2026-01")
            .param("orgId", "2")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code", is(403)));
  }

  @Test
  void admin_can_query_other_org_stats() throws Exception {
    String token = loginAndGetToken("admin", "123456");

    mockMvc.perform(get("/api/stats/check-in")
            .param("from", "2026-01")
            .param("to", "2026-01")
            .param("orgId", "2")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is(0)))
        .andExpect(jsonPath("$.data.totalAdmissions", is(1)))
        .andExpect(jsonPath("$.data.totalDischarges", is(1)));
  }

  @Test
  void export_elder_flow_report_returns_csv() throws Exception {
    String token = loginAndGetToken("admin", "123456");

    mockMvc.perform(get("/api/stats/elder-flow-report/export")
            .param("fromDate", "2026-01-01")
            .param("toDate", "2026-01-31")
            .param("orgId", "2")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("text/csv")))
        .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment;")));
  }

  @Test
  @Sql(statements = {
      "INSERT INTO bill_monthly (id, org_id, elder_id, bill_month, total_amount, paid_amount, outstanding_amount, status, is_deleted) "
          + "VALUES (8101, 1, 200, '2026-01', 120.00, 120.00, 0.00, 2, 0)",
      "INSERT INTO bill_monthly (id, org_id, elder_id, bill_month, total_amount, paid_amount, outstanding_amount, status, is_deleted) "
          + "VALUES (8102, 1, 201, '2026-01', 80.00, 80.00, 0.00, 2, 0)",
      "INSERT INTO `order` (id, org_id, order_no, elder_id, total_amount, points_used, payable_amount, pay_status, order_status, create_time, update_time, is_deleted) "
          + "VALUES (8201, 1, 'O-200', 200, 30.00, 0, 30.00, 1, 1, TIMESTAMP '2026-01-15 10:00:00', TIMESTAMP '2026-01-15 10:00:00', 0)",
      "INSERT INTO `order` (id, org_id, order_no, elder_id, total_amount, points_used, payable_amount, pay_status, order_status, create_time, update_time, is_deleted) "
          + "VALUES (8202, 1, 'O-201', 201, 50.00, 0, 50.00, 1, 1, TIMESTAMP '2026-01-20 10:00:00', TIMESTAMP '2026-01-20 10:00:00', 0)"
  })
  void consumption_supports_elder_filter() throws Exception {
    String token = loginAndGetToken("admin", "123456");

    mockMvc.perform(get("/api/stats/consumption")
            .param("from", "2026-01")
            .param("to", "2026-01")
            .param("elderId", "200")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is(0)))
        .andExpect(jsonPath("$.data.totalBillConsumption", is(120.00)))
        .andExpect(jsonPath("$.data.totalStoreConsumption", is(30.00)))
        .andExpect(jsonPath("$.data.totalConsumption", is(150.00)))
        .andExpect(jsonPath("$.data.topConsumerElders.length()", is(1)))
        .andExpect(jsonPath("$.data.topConsumerElders[0].elderId", is(200)));
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
