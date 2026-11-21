package com.zhiyangyun.care.digitaltwin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

/** 运营大屏：只给院长、管理员；数字来自真实业务表，没有数据源的块标为未接入而不是填 0 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DigitalTwinScreenControllerTest {
  private static final String HASH = "$2a$10$oHe4Uo7TeO5Ftwj0zK33auA8wXIMcbHScVrPTYbeTnrrrkbEJX.Qe";

  @Autowired private MockMvc mvc;
  @Autowired private ObjectMapper json;
  @Autowired private JdbcTemplate jdbc;

  @BeforeEach
  void seed() {
    Object[][] users = {{96101L, "scr_director", 96111L, "DIRECTOR"}, {96102L, "scr_nurse", 96112L, "NURSING_EMPLOYEE"}};
    for (Object[] u : users) {
      jdbc.update("MERGE INTO staff (id, org_id, department_id, staff_no, username, password_hash, real_name, status) KEY(id) VALUES (?, 1, 10, ?, ?, ?, ?, 1)",
          u[0], "S" + u[0], u[1], HASH, u[1]);
      jdbc.update("MERGE INTO role (id, org_id, role_name, role_code, status) KEY(id) VALUES (?, 1, ?, ?, 1)", u[2], u[3], u[3]);
      jdbc.update("MERGE INTO staff_role (id, org_id, staff_id, role_id) KEY(id) VALUES (?, 1, ?, ?)", (Long) u[0] + 100, u[0], u[2]);
    }
    jdbc.update("MERGE INTO elder (id, org_id, full_name, care_level, status) KEY(id) VALUES "
        + "(96051, 1, '大屏测试甲', '大屏测试等级', 1), (96052, 1, '大屏测试乙', 'SCR_L1', 1), (96053, 1, '大屏测试丙', '大屏测试等级', 0)");
    jdbc.update("MERGE INTO care_level (id, tenant_id, org_id, level_code, level_name) KEY(id) VALUES (96061, 1, 1, 'SCR_L1', '大屏测试等级')");
  }

  @AfterEach
  void cleanup() {
    jdbc.update("DELETE FROM elder WHERE id IN (96051, 96052, 96053)");
    jdbc.update("DELETE FROM care_level WHERE id = 96061");
  }

  @Test
  void directorSeesRealAggregatesAndOthersAreForbidden() throws Exception {
    mvc.perform(get("/api/digital-twin/screen/overview").header("Authorization", "Bearer " + login("scr_nurse")))
        .andExpect(status().isForbidden());

    JsonNode data = json.readTree(mvc.perform(get("/api/digital-twin/screen/overview")
            .header("Authorization", "Bearer " + login("scr_director")))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).path("data");

    // 护理等级分布只算在住（status=1）的长者；存编码 SCR_L1 的按字典换成名称合并：测试等级 2 人，离院的不算
    long testLevel = 0;
    for (JsonNode item : data.path("careLevels").path("items")) {
      if ("大屏测试等级".equals(item.path("name").asText())) testLevel = item.path("count").asLong();
    }
    assertThat(testLevel).isEqualTo(2);
    // 每块都标明数据来源
    for (String block : new String[] {"careLevels", "alerts", "tasks", "workOrders", "health", "energy", "cameras"}) {
      assertThat(data.path(block).path("source").asText()).as(block).isNotBlank();
    }
    // 没有抄表数据时标记未接入，而不是给出 0 度电
    if (jdbc.queryForObject("SELECT COUNT(*) FROM finance_electricity_reading WHERE org_id = 1 AND is_deleted = 0", Long.class) == 0) {
      assertThat(data.path("energy").path("connected").asBoolean()).isFalse();
      assertThat(data.path("energy").has("usageKwh")).isFalse();
    }
    assertThat(data.path("health").path("hourlyMeasured").size()).isEqualTo(24);
  }

  @Test
  void executiveCockpitIsReachableUnderApiPrefix() throws Exception {
    // nginx 只转发 /api/**；驾驶舱必须在 /api 下才能在生产上取到数据
    // 测试库没有驾驶舱快照表，这里只验证路由在 /api 下存在且按角色放行
    int code = mvc.perform(get("/api/stats/executive/overview").header("Authorization", "Bearer " + login("scr_director")))
        .andReturn().getResponse().getStatus();
    assertThat(code).isNotIn(401, 403, 404);
    mvc.perform(get("/api/stats/executive/overview").header("Authorization", "Bearer " + login("scr_nurse")))
        .andExpect(status().isForbidden());
  }

  private String login(String username) throws Exception {
    String response = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"" + username + "\",\"password\":\"123456\"}"))
        .andReturn().getResponse().getContentAsString();
    return json.readTree(response).path("data").path("token").asText();
  }
}
