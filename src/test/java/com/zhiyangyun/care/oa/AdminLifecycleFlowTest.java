package com.zhiyangyun.care.oa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.model.LoginRequest;
import com.zhiyangyun.care.family.service.FamilyPortalService;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** 行政管理：家属逐项授权/有效期/撤销、一卡通生命周期、任务验收、公告阅读回执、制度文档版本审核 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminLifecycleFlowTest {
  private static final String HASH = "$2a$10$oHe4Uo7TeO5Ftwj0zK33auA8wXIMcbHScVrPTYbeTnrrrkbEJX.Qe";

  @Autowired private MockMvc mvc;
  @Autowired private ObjectMapper json;
  @Autowired private JdbcTemplate jdbc;
  @Autowired private FamilyPortalService portal;

  @BeforeEach
  void seed() {
    String[][] users = {{"97101", "adm_min", "行政部长", "97111", "HR_MINISTER"},
        {"97102", "adm_emp", "行政专员", "97112", "HR_EMPLOYEE"}};
    for (String[] u : users) {
      jdbc.update("MERGE INTO staff (id, org_id, department_id, staff_no, username, password_hash, real_name, status) KEY(id) VALUES (?, 1, 10, ?, ?, ?, ?, 1)",
          Long.valueOf(u[0]), "A" + u[0], u[1], HASH, u[2]);
      jdbc.update("MERGE INTO role (id, org_id, role_name, role_code, status) KEY(id) VALUES (?, 1, ?, ?, 1)", Long.valueOf(u[3]), u[4], u[4]);
      jdbc.update("MERGE INTO staff_role (id, org_id, staff_id, role_id) KEY(id) VALUES (?, 1, ?, ?)", Long.valueOf(u[3]) + 100, Long.valueOf(u[0]), Long.valueOf(u[3]));
    }
    jdbc.update("INSERT INTO elder_family (id, org_id, elder_id, family_user_id, relation, is_primary) VALUES (97201, 1, 97202, 97203, '女儿', 1)");
    jdbc.update("INSERT INTO one_card_account (id, tenant_id, org_id, elder_id, card_no, status, loss_flag) VALUES (97301, 1, 1, 97202, 'C-OLD-1', 'ACTIVE', 0)");
  }

  @AfterEach
  void cleanup() {
    jdbc.update("DELETE FROM elder_family WHERE id = 97201");
    jdbc.update("DELETE FROM one_card_account WHERE elder_id = 97202");
    jdbc.update("DELETE FROM oa_task WHERE title = '协同-测试任务'");
    jdbc.update("DELETE FROM oa_notice_read WHERE notice_id IN (SELECT id FROM oa_notice WHERE title = '测试公告')");
    jdbc.update("DELETE FROM oa_notice WHERE title = '测试公告'");
    jdbc.update("DELETE FROM oa_document WHERE name = '消防管理制度.pdf'");
  }

  @Test
  void familyDataFollowsItemisedAuthorisationExpiryAndRevocation() throws Exception {
    String admin = login("adm_min");
    // 只授权健康信息：费用明细被拒绝
    mvc.perform(auth(put("/api/admin/family/relations/97201/authorization"), admin)
        .content(json.writeValueAsString(Map.of("scopes", java.util.List.of("HEALTH", "BOGUS"))))).andExpect(status().isOk());
    assertThat(jdbc.queryForObject("SELECT auth_scopes FROM elder_family WHERE id = 97201", String.class)).isEqualTo("HEALTH");
    assertThatThrownBy(() -> portal.getPaymentSummary(1L, 97203L, 97202L, null)).hasMessageContaining("费用明细");
    // 有效期不能设在过去；到期后视同未绑定
    mvc.perform(auth(put("/api/admin/family/relations/97201/authorization"), admin)
        .content("{\"scopes\":[\"BILLING\"],\"validUntil\":\"" + LocalDate.now().minusDays(1) + "\"}")).andExpect(status().isBadRequest());
    jdbc.update("UPDATE elder_family SET auth_scopes = 'BILLING', valid_until = ? WHERE id = 97201", LocalDate.now().minusDays(1));
    assertThatThrownBy(() -> portal.getPaymentSummary(1L, 97203L, 97202L, null)).hasMessageContaining("请先绑定");
    // 撤销留档
    mvc.perform(auth(delete("/api/admin/family/relations/97201").param("reason", "长者本人要求"), admin)).andExpect(status().isOk());
    Map<String, Object> row = jdbc.queryForMap("SELECT is_deleted, revoke_reason, revoked_by_name FROM elder_family WHERE id = 97201");
    assertThat(row.get("IS_DELETED").toString()).isEqualTo("1");
    assertThat(row.get("REVOKE_REASON")).isEqualTo("长者本人要求");
    assertThat(row.get("REVOKED_BY_NAME")).isEqualTo("adm_min");
  }

  @Test
  void cardGoesThroughLossReissueAndActivationWithoutTouchingFunds() throws Exception {
    String admin = login("adm_emp");
    mvc.perform(auth(post("/api/card/account/97301/lifecycle"), admin).content("{\"action\":\"REPORT_LOSS\"}"))
        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message", containsString("原因")));
    mvc.perform(auth(post("/api/card/account/97301/lifecycle"), admin).content("{\"action\":\"REPORT_LOSS\",\"reason\":\"家属报失\"}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.data.status", is("LOST")));
    // 行政不能充值
    mvc.perform(auth(post("/api/card/account/recharge"), admin).content("{\"cardAccountId\":97301,\"amount\":10}")).andExpect(status().isForbidden());
    String response = mvc.perform(auth(post("/api/card/account/97301/lifecycle"), admin).content("{\"action\":\"REISSUE\",\"newCardNo\":\"C-NEW-1\"}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.data.status", is("PENDING")))
        .andExpect(jsonPath("$.data.prevCardId", is("97301"))).andReturn().getResponse().getContentAsString();
    String newId = data(response).path("id").asText();
    assertThat(jdbc.queryForObject("SELECT status FROM one_card_account WHERE id = 97301", String.class)).isEqualTo("REPLACED");
    // 旧卡已作废，不能再解挂；新卡激活后可用
    mvc.perform(auth(post("/api/card/account/97301/lifecycle"), admin).content("{\"action\":\"CANCEL_LOSS\"}")).andExpect(status().isBadRequest());
    mvc.perform(auth(post("/api/card/account/" + newId + "/lifecycle"), admin).content("{\"action\":\"ACTIVATE\"}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.data.status", is("ACTIVE")));
    // 一人仅一张在用卡
    mvc.perform(auth(post("/api/card/account"), admin).content("{\"elderId\":97202}"))
        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message", containsString("在用")));
  }

  @Test
  void taskIsAcceptedByItsCreatorAfterSubmission() throws Exception {
    String admin = login("adm_min");
    String created = mvc.perform(auth(post("/api/oa/task"), admin)
        .content("{\"title\":\"协同-测试任务\",\"assigneeId\":97101,\"assigneeName\":\"行政部长\"}"))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    String id = data(created).path("id").asText();
    mvc.perform(auth(put("/api/oa/task/" + id + "/accept"), admin)).andExpect(status().isBadRequest());
    mvc.perform(auth(put("/api/oa/task/" + id + "/submit"), admin)).andExpect(jsonPath("$.data.status", is("SUBMITTED")));
    mvc.perform(auth(put("/api/oa/task/" + id + "/accept").param("pass", "false"), admin)).andExpect(jsonPath("$.data.status", is("OPEN")));
    mvc.perform(auth(put("/api/oa/task/" + id + "/submit"), admin)).andExpect(status().isOk());
    mvc.perform(auth(put("/api/oa/task/" + id + "/accept"), admin)).andExpect(jsonPath("$.data.status", is("DONE")));
  }

  @Test
  void noticeReadReceiptCountsEachStaffOnce() throws Exception {
    String admin = login("adm_min");
    String created = mvc.perform(auth(post("/api/oa/notice"), admin)
        .content("{\"title\":\"测试公告\",\"content\":\"本周消防演练\",\"status\":\"PUBLISHED\",\"audience\":\"STAFF\"}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.data.audience", is("STAFF"))).andReturn().getResponse().getContentAsString();
    String id = data(created).path("id").asText();
    mvc.perform(auth(post("/api/oa/notice/" + id + "/read"), admin)).andExpect(status().isOk());
    mvc.perform(auth(post("/api/oa/notice/" + id + "/read"), admin)).andExpect(status().isOk());
    mvc.perform(auth(get("/api/oa/notice/" + id + "/reads"), admin))
        .andExpect(jsonPath("$.data.readCount", is(1))).andExpect(jsonPath("$.data.readers[0].staffName", is("adm_min")));
    mvc.perform(auth(post("/api/oa/notice"), admin).content("{\"title\":\"测试公告\",\"content\":\"x\",\"audience\":\"EVERYONE\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void documentNewVersionReplacesTheCurrentOneOnlyAfterReview() throws Exception {
    String emp = login("adm_emp");
    String min = login("adm_min");
    String created = mvc.perform(auth(post("/api/oa/document"), emp)
        .content("{\"name\":\"消防管理制度.pdf\",\"url\":\"/uploads/v1.pdf\"}"))
        .andExpect(jsonPath("$.data.reviewStatus", is("PENDING"))).andReturn().getResponse().getContentAsString();
    String v1 = data(created).path("id").asText();
    mvc.perform(auth(put("/api/oa/document/" + v1 + "/review"), emp).content("{\"pass\":true}")).andExpect(status().isForbidden());
    mvc.perform(auth(put("/api/oa/document/" + v1 + "/review"), min).content("{\"pass\":true}")).andExpect(jsonPath("$.data.reviewStatus", is("APPROVED")));
    // 已审核的文件不能静默替换
    mvc.perform(auth(put("/api/oa/document/" + v1), emp).content("{\"name\":\"消防管理制度.pdf\",\"url\":\"/uploads/other.pdf\"}"))
        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message", containsString("新版本")));
    String next = mvc.perform(auth(post("/api/oa/document/" + v1 + "/versions"), emp).content("{\"url\":\"/uploads/v2.pdf\"}"))
        .andExpect(jsonPath("$.data.versionNo", is(2))).andExpect(jsonPath("$.data.isCurrent", is(0)))
        .andReturn().getResponse().getContentAsString();
    String v2 = data(next).path("id").asText();
    mvc.perform(auth(put("/api/oa/document/" + v2 + "/review"), min).content("{\"pass\":false}")).andExpect(status().isBadRequest());
    mvc.perform(auth(put("/api/oa/document/" + v2 + "/review"), min).content("{\"pass\":true,\"note\":\"同意\"}")).andExpect(status().isOk());
    assertThat(jdbc.queryForObject("SELECT is_current FROM oa_document WHERE id = ?", Integer.class, Long.valueOf(v1))).isZero();
    assertThat(jdbc.queryForObject("SELECT is_current FROM oa_document WHERE id = ?", Integer.class, Long.valueOf(v2))).isEqualTo(1);
    mvc.perform(auth(get("/api/oa/document/" + v2 + "/versions"), emp)).andExpect(jsonPath("$.data.length()", is(2)));
  }

  private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder builder, String token) {
    return builder.header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON);
  }

  private JsonNode data(String response) throws Exception {
    return json.readTree(response).path("data");
  }

  private String login(String username) throws Exception {
    LoginRequest request = new LoginRequest();
    request.setUsername(username);
    request.setPassword("123456");
    String response = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request)))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    return json.readTree(response).path("data").path("token").asText();
  }
}
