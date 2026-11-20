package com.zhiyangyun.care.digitaltwin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.model.LoginRequest;
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

/** 数字孪生：只读接口按机构隔离；绑定只有管理员能建/发布并写审计；篡改 assetId / 楼栋 ID / 绑定 ID 读不到别家数据 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DigitalTwinControllerTest {
  private static final String HASH = "$2a$10$oHe4Uo7TeO5Ftwj0zK33auA8wXIMcbHScVrPTYbeTnrrrkbEJX.Qe";

  @Autowired private MockMvc mvc;
  @Autowired private ObjectMapper json;
  @Autowired private JdbcTemplate jdbc;

  @BeforeEach
  void seed() {
    // staffId, username, orgId, roleId, roleCode
    Object[][] users = {{95101L, "dt_admin", 1L, 95111L, "ADMIN"}, {95102L, "dt_nurse", 1L, 95112L, "NURSING_EMPLOYEE"},
        {95103L, "dt_fin", 1L, 95113L, "FINANCE_EMPLOYEE"}, {95104L, "dt_admin2", 2L, 95114L, "ADMIN"},
        {95105L, "dt_nurse2", 2L, 95115L, "NURSING_EMPLOYEE"}};
    for (Object[] u : users) {
      jdbc.update("MERGE INTO staff (id, org_id, department_id, staff_no, username, password_hash, real_name, status) KEY(id) VALUES (?, ?, 10, ?, ?, ?, ?, 1)",
          u[0], u[2], "D" + u[0], u[1], HASH, u[1]);
      jdbc.update("MERGE INTO role (id, org_id, role_name, role_code, status) KEY(id) VALUES (?, ?, ?, ?, 1)", u[3], u[2], u[4], u[4]);
      jdbc.update("MERGE INTO staff_role (id, org_id, staff_id, role_id) KEY(id) VALUES (?, ?, ?, ?)", (Long) u[0] + 100, u[2], u[0], u[3]);
    }
    jdbc.update("MERGE INTO building (id, tenant_id, org_id, name, code) KEY(id) VALUES (95001, 1, 1, '一号楼', 'B1'), (95002, 2, 2, '别家楼', 'X1')");
    jdbc.update("MERGE INTO room (id, org_id, building_id, room_no) KEY(id) VALUES (95011, 1, 95001, 'DT101'), (95012, 2, 95002, 'DT201')");
    jdbc.update("MERGE INTO bed (id, org_id, room_id, bed_no, bed_qr_code, status) KEY(id) VALUES "
        + "(95021, 1, 95011, '01', 'DTQ1', 2), (95022, 1, 95011, '02', 'DTQ2', 1), (95023, 2, 95012, '01', 'DTQ3', 2)");
  }

  @AfterEach
  void cleanup() {
    jdbc.update("DELETE FROM dt_business_binding WHERE created_by BETWEEN 95101 AND 95105");
    jdbc.update("DELETE FROM audit_log WHERE entity_type = 'DT_BUSINESS_BINDING'");
    jdbc.update("DELETE FROM bed WHERE id IN (95021, 95022, 95023)");
    jdbc.update("DELETE FROM room WHERE id IN (95011, 95012)");
    jdbc.update("DELETE FROM building WHERE id IN (95001, 95002)");
  }

  @Test
  void conceptSceneIsReadableAndClearlyMarkedDemo() throws Exception {
    String nurse = login("dt_nurse");
    mvc.perform(auth(get("/api/digital-twin/scene"), nurse)).andExpect(status().isOk())
        .andExpect(jsonPath("$.data.dataMode", is("DEMO"))).andExpect(jsonPath("$.data.verificationStatus", is("CONCEPT")))
        .andExpect(jsonPath("$.data.canManageBindings", is(false)));
    JsonNode buildings = data(mvc.perform(auth(get("/api/digital-twin/assets").param("type", "BUILDING"), nurse)).andReturn());
    assertThat(buildings.size()).isEqualTo(10);
    assertThat(buildings.get(0).path("assetId").asText()).isEqualTo("GF-BLD-TMP-001");
    assertThat(buildings.get(0).path("modelNodeKey").asText()).isEqualTo("BLD_TMP_001");
    assertThat(buildings.get(0).path("verificationStatus").asText()).isEqualTo("CONCEPT");
    mvc.perform(auth(get("/api/digital-twin/assets/GF-BLD-TMP-999"), nurse)).andExpect(status().isNotFound());
    mvc.perform(auth(get("/api/digital-twin/assets/GF-BLD-TMP-001/summary").param("mode", "nope"), nurse)).andExpect(status().isBadRequest());
    mvc.perform(auth(get("/api/digital-twin/assets/GF-BLD-TMP-001/summary").param("mode", "fire"), nurse))
        .andExpect(jsonPath("$.data.dataMode", is("NOT_CONNECTED"))).andExpect(jsonPath("$.data.metrics.length()", is(0)));
  }

  @Test
  void onlyAdminsBindAndOnlyBedRolesSeeOccupancy() throws Exception {
    String nurse = login("dt_nurse");
    String fin = login("dt_fin");
    String body = "{\"assetId\":\"GF-BLD-TMP-001\",\"entityType\":\"BUILDING\",\"externalEntityId\":95001}";
    mvc.perform(auth(post("/api/digital-twin/admin/bindings"), nurse).content(body)).andExpect(status().isForbidden());
    mvc.perform(auth(get("/api/digital-twin/admin/bindings"), nurse)).andExpect(status().isForbidden());
    mvc.perform(auth(get("/api/digital-twin/assets/GF-CAMPUS-001/summary").param("mode", "occupancy"), fin)).andExpect(status().isForbidden());
    mvc.perform(auth(get("/api/digital-twin/assets/GF-CAMPUS-001/summary").param("mode", "video"), fin)).andExpect(status().isOk());

    String admin = login("dt_admin");
    // 未绑定前不显示数字
    mvc.perform(auth(get("/api/digital-twin/assets/GF-BLD-TMP-001/summary").param("mode", "occupancy"), nurse))
        .andExpect(jsonPath("$.data.dataMode", is("UNBOUND")));
    String id = data(mvc.perform(auth(post("/api/digital-twin/admin/bindings"), admin).content(body))
        .andExpect(status().isOk()).andExpect(jsonPath("$.data.bindingStatus", is("PENDING"))).andReturn()).path("id").asText();
    mvc.perform(auth(get("/api/digital-twin/assets/GF-BLD-TMP-001/summary").param("mode", "occupancy"), nurse))
        .andExpect(jsonPath("$.data.dataMode", is("UNBOUND")));
    mvc.perform(auth(post("/api/digital-twin/admin/bindings/" + id + "/publish"), admin)).andExpect(jsonPath("$.data.bindingStatus", is("ACTIVE")));

    JsonNode summary = data(mvc.perform(auth(get("/api/digital-twin/assets/GF-BLD-TMP-001/summary").param("mode", "occupancy"), nurse))
        .andExpect(jsonPath("$.data.dataMode", is("LIVE"))).andReturn());
    assertThat(metric(summary, "totalBeds")).isEqualTo(2);
    assertThat(metric(summary, "occupiedBeds")).isEqualTo(1);
    assertThat(metric(summary, "availableBeds")).isEqualTo(1);
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE entity_type = 'DT_BUSINESS_BINDING' AND action_type IN ('DT_BINDING_CREATE','DT_BINDING_PUBLISH')",
        Integer.class)).isEqualTo(2);

    // 同一楼栋再发布一条到另一个资产：旧绑定自动停用，并留审计
    String second = data(mvc.perform(auth(post("/api/digital-twin/admin/bindings"), admin)
        .content("{\"assetId\":\"GF-BLD-TMP-002\",\"entityType\":\"BUILDING\",\"externalEntityId\":95001}")).andReturn()).path("id").asText();
    mvc.perform(auth(post("/api/digital-twin/admin/bindings/" + second + "/publish"), admin)).andExpect(status().isOk());
    assertThat(jdbc.queryForObject("SELECT binding_status FROM dt_business_binding WHERE id = ?", String.class, Long.valueOf(id))).isEqualTo("RETIRED");
    mvc.perform(auth(get("/api/digital-twin/assets/GF-BLD-TMP-001/summary").param("mode", "occupancy"), nurse))
        .andExpect(jsonPath("$.data.dataMode", is("UNBOUND")));
  }

  @Test
  void tamperedIdsCannotReachAnotherOrganization() throws Exception {
    String admin = login("dt_admin");
    String admin2 = login("dt_admin2");
    // 绑定到别家机构的楼栋 ID：当作不存在
    mvc.perform(auth(post("/api/digital-twin/admin/bindings"), admin)
        .content("{\"assetId\":\"GF-BLD-TMP-001\",\"entityType\":\"BUILDING\",\"externalEntityId\":95002}")).andExpect(status().isNotFound());
    // 不支持的实体类型（例如直接绑长者）被拒
    mvc.perform(auth(post("/api/digital-twin/admin/bindings"), admin)
        .content("{\"assetId\":\"GF-BLD-TMP-001\",\"entityType\":\"ELDER\",\"externalEntityId\":200}")).andExpect(status().isBadRequest());

    String id = data(mvc.perform(auth(post("/api/digital-twin/admin/bindings"), admin)
        .content("{\"assetId\":\"GF-BLD-TMP-001\",\"entityType\":\"BUILDING\",\"externalEntityId\":95001}")).andReturn()).path("id").asText();
    mvc.perform(auth(post("/api/digital-twin/admin/bindings/" + id + "/publish"), admin)).andExpect(status().isOk());

    // 别家管理员拿到绑定 ID 也看不到、发布不了、停用不了
    mvc.perform(auth(get("/api/digital-twin/admin/bindings"), admin2)).andExpect(jsonPath("$.data.length()", is(0)));
    mvc.perform(auth(post("/api/digital-twin/admin/bindings/" + id + "/retire"), admin2)).andExpect(status().isNotFound());
    mvc.perform(auth(post("/api/digital-twin/admin/bindings/" + id + "/publish"), admin2)).andExpect(status().isNotFound());

    // 同一个 assetId 在别家机构解析为别家自己的资产：看不到本机构绑定楼栋的床位
    String nurse2 = login("dt_nurse2");
    mvc.perform(auth(get("/api/digital-twin/assets/GF-BLD-TMP-001/summary").param("mode", "occupancy"), nurse2))
        .andExpect(jsonPath("$.data.dataMode", is("UNBOUND")));
    JsonNode campus2 = data(mvc.perform(auth(get("/api/digital-twin/assets/GF-CAMPUS-001/summary").param("mode", "occupancy"), nurse2)).andReturn());
    long org2Beds = jdbc.queryForObject("SELECT COUNT(*) FROM bed WHERE org_id = 2 AND is_deleted = 0", Long.class);
    assertThat(metric(campus2, "totalBeds")).isEqualTo(org2Beds);
    assertThat(jdbc.queryForObject("SELECT binding_status FROM dt_business_binding WHERE id = ?", String.class, Long.valueOf(id))).isEqualTo("ACTIVE");
  }

  private long metric(JsonNode summary, String key) {
    for (JsonNode m : summary.path("metrics")) {
      if (key.equals(m.path("key").asText())) {
        return m.path("value").asLong();
      }
    }
    throw new AssertionError("missing metric " + key);
  }

  private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder builder, String token) {
    return builder.header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON);
  }

  private JsonNode data(org.springframework.test.web.servlet.MvcResult result) throws Exception {
    return json.readTree(result.getResponse().getContentAsString()).path("data");
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
