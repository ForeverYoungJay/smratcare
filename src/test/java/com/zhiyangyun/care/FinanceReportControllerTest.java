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
class FinanceReportControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @Sql(statements = {
      "UPDATE room SET building = 'A栋', floor_no = '1层' WHERE id = 11",
      "UPDATE bed SET elder_id = NULL, status = 2 WHERE id = 101",
      "UPDATE elder SET bed_id = NULL, lifecycle_status = 'IN_HOSPITAL' WHERE id = 201",
      "INSERT INTO elder_bed_relation (id, org_id, elder_id, bed_id, start_date, active_flag, is_deleted) "
          + "VALUES (9201, 1, 201, 101, DATE '2026-01-01', 1, 0)",
      "INSERT INTO bill_monthly (id, org_id, elder_id, bill_month, total_amount, paid_amount, outstanding_amount, status, is_deleted) "
          + "VALUES (9202, 1, 201, '2026-01', 88.00, 88.00, 0.00, 2, 0)",
      "INSERT INTO payment_record (id, org_id, bill_monthly_id, amount, pay_method, external_txn_id, paid_at, operator_staff_id, is_deleted) "
          + "VALUES (9203, 1, 9202, 88.00, 'CASH', 'TXN-9203', TIMESTAMP '2026-01-12 09:30:00', 500, 0)"
  })
  void entrySummary_uses_active_relation_when_bed_projection_is_empty() throws Exception {
    String token = loginAndGetToken("admin", "123456");

    mockMvc.perform(get("/api/finance/report/entry-summary")
            .param("reportKey", "FLOOR_ROOM")
            .param("from", "2026-01")
            .param("to", "2026-01")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is(0)))
        .andExpect(jsonPath("$.data.topRooms[0].label", is("A栋-1层-A102")))
        .andExpect(jsonPath("$.data.topRooms[0].amount", is(88.00)));
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
