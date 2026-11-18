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
  // 测试共用一个 H2 库：先清掉其他用例给同一长者同月生成的账单，避免唯一键冲突和金额被叠加
  @Sql(statements = {
      "DELETE FROM payment_record WHERE bill_monthly_id IN (SELECT id FROM bill_monthly WHERE org_id = 1 AND elder_id = 201 AND bill_month = '2026-01')",
      "DELETE FROM bill_monthly WHERE org_id = 1 AND elder_id = 201 AND bill_month = '2026-01'",
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
  @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = {
      "DELETE FROM payment_record WHERE id = 9203",
      "DELETE FROM bill_monthly WHERE id = 9202",
      "DELETE FROM elder_bed_relation WHERE id = 9201"
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

  @Test
  @Sql(statements = {
      "DELETE FROM payment_record WHERE bill_monthly_id IN (SELECT id FROM bill_monthly WHERE org_id = 1 AND elder_id = 201 AND bill_month = '2025-02')",
      "DELETE FROM bill_monthly WHERE org_id = 1 AND elder_id = 201 AND bill_month = '2025-02'",
      "INSERT INTO bill_monthly (id, org_id, elder_id, bill_month, total_amount, paid_amount, outstanding_amount, status, is_deleted) "
          + "VALUES (9212, 1, 201, '2025-02', 66.00, 66.00, 0.00, 2, 0)",
      "INSERT INTO payment_record (id, org_id, bill_monthly_id, amount, pay_method, external_txn_id, paid_at, operator_staff_id, is_deleted) "
          + "VALUES (9213, 1, 9212, 66.00, 'CASH', 'TXN-9213', TIMESTAMP '2025-02-12 09:30:00', 500, 0)"
  })
  @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = {
      "DELETE FROM payment_record WHERE id = 9213",
      "DELETE FROM bill_monthly WHERE id = 9212"
  })
  void paymentPage_shows_which_elder_and_bill_month_was_paid() throws Exception {
    String token = loginAndGetToken("admin", "123456");

    mockMvc.perform(get("/api/finance/payment/page")
            .param("billId", "9212")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.records[0].elderName", is("Elder Two")))
        .andExpect(jsonPath("$.data.records[0].billMonth", is("2025-02")));
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
