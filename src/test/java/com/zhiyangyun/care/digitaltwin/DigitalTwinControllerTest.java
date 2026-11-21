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
        {95105L, "dt_nurse2", 2L, 95115L, "NURSING_EMPLOYEE"}, {95106L, "dt_logi", 1L, 95116L, "LOGISTICS_EMPLOYEE"}};
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
    jdbc.update("MERGE INTO floor (id, tenant_id, org_id, building_id, floor_no) KEY(id) VALUES (95031, 1, 1, 95001, '1F'), (95032, 1, 1, 95001, '2F')");
    jdbc.update("MERGE INTO life_maintenance_request (id, org_id, room_id, status, priority, is_deleted) KEY(id) VALUES "
        + "(95041, 1, 95011, 'OPEN', 'HIGH', 0), (95042, 1, 95011, 'COMPLETED', 'NORMAL', 0), (95043, 2, 95012, 'OPEN', 'HIGH', 0)");
  }

  /** 手工绑定规则要用尚未绑定的楼栋资产；演示占位已从库里删除，这里按需插入测试专用行 */
  private void unboundAsset(long orgId, String code) {
    jdbc.update("INSERT INTO dt_spatial_asset (org_id, asset_code, name, asset_type, parent_code, verification_status, sort_no) "
        + "VALUES (?, ?, ?, 'BUILDING', 'GF-CAMPUS-001', 'UNVERIFIED', 1)", orgId, code, "测试未绑定楼栋 " + code);
  }

  @AfterEach
  void cleanup() {
    jdbc.update("DELETE FROM dt_business_binding WHERE asset_code LIKE 'GF-BLD-TMP-%'");
    jdbc.update("DELETE FROM dt_spatial_asset WHERE asset_code LIKE 'GF-BLD-TMP-%'");
    jdbc.update("DELETE FROM dt_business_binding WHERE created_by BETWEEN 95101 AND 95106");
    jdbc.update("DELETE FROM audit_log WHERE entity_type = 'DT_BUSINESS_BINDING'");
    jdbc.update("DELETE FROM dt_spatial_asset WHERE asset_code LIKE 'GF-BLD-B%'");
    jdbc.update("DELETE FROM life_maintenance_request WHERE id IN (95041, 95042, 95043)");
    jdbc.update("DELETE FROM floor WHERE id IN (95031, 95032)");
    jdbc.update("DELETE FROM bed WHERE id IN (95021, 95022, 95023)");
    jdbc.update("DELETE FROM room WHERE id IN (95011, 95012)");
    jdbc.update("DELETE FROM building WHERE id IN (95001, 95002)");
  }

  @Test
  void noDemoDataAndRealBuildingsAppearWithoutAdminSync() throws Exception {
    String nurse = login("dt_nurse");
    mvc.perform(auth(get("/api/digital-twin/scene"), nurse)).andExpect(status().isOk())
        .andExpect(jsonPath("$.data.dataMode", is("LIVE"))).andExpect(jsonPath("$.data.canManageBindings", is(false)));
    // 普通员工打开即可看到本机构真实楼栋（自动接入主数据），没有示例占位楼栋
    JsonNode buildings = data(mvc.perform(auth(get("/api/digital-twin/assets").param("type", "BUILDING"), nurse)).andReturn());
    JsonNode real = null;
    for (JsonNode b : buildings) {
      assertThat(b.path("assetId").asText()).doesNotStartWith("GF-BLD-TMP-");
      assertThat(b.path("bound").asBoolean()).isTrue();
      if ("GF-BLD-B95001".equals(b.path("assetId").asText())) real = b;
    }
    assertThat(real).isNotNull();
    assertThat(real.path("buildingName").asText()).isEqualTo("一号楼");
    JsonNode campus = data(mvc.perform(auth(get("/api/digital-twin/assets").param("type", "CAMPUS"), nurse)).andReturn());
    assertThat(campus.get(0).path("displayName").asText()).isEqualTo("园区");
    mvc.perform(auth(get("/api/digital-twin/assets/GF-BLD-TMP-001"), nurse)).andExpect(status().isNotFound());
    mvc.perform(auth(get("/api/digital-twin/assets/GF-BLD-B95001/summary").param("mode", "nope"), nurse)).andExpect(status().isBadRequest());
    // 消防汇总只开放给后勤口径；护理员 403
    mvc.perform(auth(get("/api/digital-twin/assets/GF-CAMPUS-001/summary").param("mode", "fire"), nurse)).andExpect(status().isForbidden());
  }

  @Test
  void onlyAdminsBindAndOnlyBedRolesSeeOccupancy() throws Exception {
    unboundAsset(1L, "GF-BLD-TMP-001");
    unboundAsset(1L, "GF-BLD-TMP-002");
    String nurse = login("dt_nurse");
    String fin = login("dt_fin");
    String body = "{\"assetId\":\"GF-BLD-TMP-001\",\"entityType\":\"BUILDING\",\"externalEntityId\":95001}";
    mvc.perform(auth(post("/api/digital-twin/admin/bindings"), nurse).content(body)).andExpect(status().isForbidden());
    mvc.perform(auth(get("/api/digital-twin/admin/bindings"), nurse)).andExpect(status().isForbidden());
    mvc.perform(auth(get("/api/digital-twin/assets/GF-CAMPUS-001/summary").param("mode", "occupancy"), fin)).andExpect(status().isForbidden());
    mvc.perform(auth(get("/api/digital-twin/assets/GF-CAMPUS-001/summary").param("mode", "video"), fin)).andExpect(status().isForbidden());
    mvc.perform(auth(get("/api/digital-twin/overlay").param("mode", "occupancy"), fin)).andExpect(status().isForbidden());

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
  void adminSyncsRealBuildingsAndSummariesReadRealTables() throws Exception {
    String nurse = login("dt_nurse");
    String admin = login("dt_admin");
    String logi = login("dt_logi");
    mvc.perform(auth(post("/api/digital-twin/admin/sync-buildings"), nurse)).andExpect(status().isForbidden());
    JsonNode first = data(mvc.perform(auth(post("/api/digital-twin/admin/sync-buildings"), admin)).andExpect(status().isOk()).andReturn());
    assertThat(first.path("created").asInt()).isGreaterThanOrEqualTo(1);
    // 再同步一次不重复生成
    mvc.perform(auth(post("/api/digital-twin/admin/sync-buildings"), admin)).andExpect(jsonPath("$.data.created", is(0)));
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE action_type = 'DT_ASSET_SYNC'", Integer.class)).isGreaterThanOrEqualTo(1);

    JsonNode buildings = data(mvc.perform(auth(get("/api/digital-twin/assets").param("type", "BUILDING"), nurse)).andReturn());
    JsonNode synced = null;
    for (JsonNode b : buildings) {
      if ("GF-BLD-B95001".equals(b.path("assetId").asText())) synced = b;
      assertThat(b.path("assetId").asText()).isNotEqualTo("GF-BLD-B95002"); // 别家楼栋不会同步进来
    }
    assertThat(synced).isNotNull();
    assertThat(synced.path("buildingName").asText()).isEqualTo("一号楼");
    assertThat(synced.path("floorCount").asInt()).isEqualTo(2);
    assertThat(synced.path("roomCount").asInt()).isEqualTo(1);
    assertThat(synced.path("verificationStatus").asText()).isEqualTo("MASTER_DATA");

    // 楼栋级报修：按房间归属，只数本楼、本机构
    JsonNode facility = data(mvc.perform(auth(get("/api/digital-twin/assets/GF-BLD-B95001/summary").param("mode", "facility"), nurse))
        .andExpect(jsonPath("$.data.dataMode", is("LIVE"))).andReturn());
    assertThat(metric(facility, "openRequests")).isEqualTo(1);
    assertThat(metric(facility, "urgentRequests")).isEqualTo(1);
    JsonNode overlay = data(mvc.perform(auth(get("/api/digital-twin/overlay").param("mode", "occupancy"), nurse)).andReturn());
    boolean found = false;
    for (JsonNode o : overlay) {
      if ("GF-BLD-B95001".equals(o.path("assetId").asText())) {
        assertThat(o.path("value").asInt()).isEqualTo(50);
        found = true;
      }
    }
    assertThat(found).isTrue();
    // 消防、视频：后勤可看全园口径；楼栋级未映射时不出数字
    mvc.perform(auth(get("/api/digital-twin/assets/GF-CAMPUS-001/summary").param("mode", "fire"), logi))
        .andExpect(jsonPath("$.data.dataMode", is("LIVE")));
    mvc.perform(auth(get("/api/digital-twin/assets/GF-BLD-B95001/summary").param("mode", "video"), logi))
        .andExpect(jsonPath("$.data.dataMode", is("NOT_CONNECTED"))).andExpect(jsonPath("$.data.metrics.length()", is(0)));
  }

  @Test
  void tamperedIdsCannotReachAnotherOrganization() throws Exception {
    unboundAsset(1L, "GF-BLD-TMP-001");
    unboundAsset(2L, "GF-BLD-TMP-001");
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
