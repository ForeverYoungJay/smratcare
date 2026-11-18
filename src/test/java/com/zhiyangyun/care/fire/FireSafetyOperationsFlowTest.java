package com.zhiyangyun.care.fire;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.model.LoginRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

/** 消防安全：关闭规则、交接双方确认、报告签署、录像调阅审批与审计、隐患统计和楼栋风险（走真实接口与权限） */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FireSafetyOperationsFlowTest {
  private static final String HASH = "$2a$10$oHe4Uo7TeO5Ftwj0zK33auA8wXIMcbHScVrPTYbeTnrrrkbEJX.Qe";
  private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  @Autowired private MockMvc mvc;
  @Autowired private ObjectMapper json;
  @Autowired private JdbcTemplate jdbc;

  @BeforeEach
  void seedUsers() {
    String[][] users = {{"98101", "fire_guard", "保卫甲", "98111", "GUARD"},
        {"98102", "fire_emp", "后勤员乙", "98112", "LOGISTICS_EMPLOYEE"},
        {"98103", "fire_min", "后勤部长丙", "98113", "LOGISTICS_MINISTER"}};
    for (String[] u : users) {
      jdbc.update("MERGE INTO staff (id, org_id, department_id, staff_no, username, password_hash, real_name, status) KEY(id) VALUES (?, 1, 10, ?, ?, ?, ?, 1)",
          Long.valueOf(u[0]), "F" + u[0], u[1], HASH, u[2]);
      jdbc.update("MERGE INTO role (id, org_id, role_name, role_code, status) KEY(id) VALUES (?, 1, ?, ?, 1)", Long.valueOf(u[3]), u[4], u[4]);
      jdbc.update("MERGE INTO staff_role (id, org_id, staff_id, role_id) KEY(id) VALUES (?, 1, ?, ?)", Long.valueOf(u[3]) + 100, Long.valueOf(u[0]), Long.valueOf(u[3]));
    }
    jdbc.update("INSERT INTO fire_safety_record (id, tenant_id, org_id, record_type, title, location, inspector_name, check_time, status, issue_description, next_check_date, third_party_maintenance_file_url) "
        + "VALUES (98201, 1, 1, 'MONTHLY_CHECK', '11月检查', 'B栋二层防火门', '保卫甲', CURRENT_TIMESTAMP, 'OPEN', '闭门器异常', DATEADD('DAY', -1, CURRENT_DATE), NULL), "
        + "(98202, 1, 1, 'MAINTENANCE_REPORT', '10月维保报告', '全院', '后勤员乙', CURRENT_TIMESTAMP, 'OPEN', NULL, NULL, '/uploads/fire/report.pdf')");
    jdbc.update("INSERT INTO fire_location (id, org_id, building, floor_name, risk_level, key_part, enabled, is_deleted) VALUES (98301, 1, 'B栋', '二层', 'HIGH', 1, 1, 0), (98302, 1, 'A栋', '一层', 'LOW', 0, 1, 0)");
    jdbc.update("INSERT INTO logistics_camera (id, org_id, camera_code, camera_name, location, channel_ref, protocol, enabled, bitrate_mbps) VALUES (98401, 1, 'CAM-1', 'B栋走廊', 'B栋二层', 'ch1', 'RTSP', 1, 2)");
  }

  @AfterEach
  void cleanup() {
    for (String table : new String[] {"fire_safety_record", "fire_location", "fire_duty_handover", "fire_report_signature",
        "fire_video_access_request", "fire_video_access_log"}) {
      jdbc.update("DELETE FROM " + table + " WHERE org_id = 1");
    }
    jdbc.update("DELETE FROM logistics_camera WHERE id = 98401");
  }

  @Test
  void aHazardIsClosedOnlyByAManagerAfterHandlingIsRecorded() throws Exception {
    String guard = login("fire_guard");
    String manager = login("fire_min");
    // 一线人员不能关闭
    mvc.perform(auth(put("/api/fire/records/98201/close"), guard)).andExpect(status().isForbidden());
    // 主管也不能在没写处置措施时关闭
    mvc.perform(auth(put("/api/fire/records/98201/close"), manager))
        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message", containsString("处置措施")));
    jdbc.update("UPDATE fire_safety_record SET action_taken = '已更换闭门器并复核' WHERE id = 98201");
    mvc.perform(auth(put("/api/fire/records/98201/close"), manager)).andExpect(status().isOk());
    assertThat(jdbc.queryForObject("SELECT status FROM fire_safety_record WHERE id = 98201", String.class)).isEqualTo("CLOSED");
  }

  @Test
  void hazardStatsAndBuildingRiskComeFromOpenIssues() throws Exception {
    String guard = login("fire_guard");
    mvc.perform(auth(get("/api/fire/hazards/stats"), guard))
        .andExpect(jsonPath("$.data.total", is("1"))).andExpect(jsonPath("$.data.overdue", is("1")));
    mvc.perform(auth(get("/api/fire/buildings/risk"), guard))
        .andExpect(jsonPath("$.data[?(@.building == 'B栋')].level").value("DANGER"))
        .andExpect(jsonPath("$.data[?(@.building == 'A栋')].level").value("OK"));
    mvc.perform(auth(get("/api/fire/records/page").param("status", "ACTIVE").param("issueOnly", "true"), guard))
        .andExpect(jsonPath("$.data.total", is("1")));
  }

  @Test
  void handoverIsConfirmedOnlyByTheNamedTakeoverPerson() throws Exception {
    String guard = login("fire_guard");
    String emp = login("fire_emp");
    String body = "{\"shiftName\":\"夜班 16:00-00:00\",\"takeoverStaffId\":98102,\"checklist\":\"[]\",\"openIssues\":\"B栋探测器待修\"}";
    mvc.perform(auth(post("/api/fire/handovers"), guard).content(body.replace("98102", "98101")))
        .andExpect(status().isBadRequest());
    String id = data(mvc.perform(auth(post("/api/fire/handovers"), guard).content(body)).andReturn().getResponse().getContentAsString()).path("id").asText();
    String confirm = "{\"accept\":true}";
    mvc.perform(auth(post("/api/fire/handovers/" + id + "/confirm"), guard).content(confirm))
        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message", containsString("接班人本人")));
    mvc.perform(auth(post("/api/fire/handovers/" + id + "/confirm"), emp).content(confirm))
        .andExpect(jsonPath("$.data.status", is("CONFIRMED")));
  }

  @Test
  void aReportIsReviewedByAManagerOtherThanTheSubmitter() throws Exception {
    String manager = login("fire_min");
    String emp = login("fire_emp");
    String submit = "{\"recordId\":98202,\"signType\":\"SUBMIT\"}";
    String review = "{\"recordId\":98202,\"signType\":\"REVIEW\",\"comment\":\"已核对\"}";
    mvc.perform(auth(post("/api/fire/report-signatures"), emp).content(review)).andExpect(status().isBadRequest());
    mvc.perform(auth(post("/api/fire/report-signatures"), manager).content(submit)).andExpect(status().isOk());
    mvc.perform(auth(post("/api/fire/report-signatures"), manager).content(review))
        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message", containsString("提交人不能审核")));
    jdbc.update("DELETE FROM fire_report_signature WHERE record_id = 98202");
    mvc.perform(auth(post("/api/fire/report-signatures"), emp).content(submit)).andExpect(status().isOk());
    mvc.perform(auth(post("/api/fire/report-signatures"), manager).content(review)).andExpect(jsonPath("$.data.signType", is("REVIEW")));
  }

  @Test
  void recordingsAreVisibleOnlyToTheApplicantAfterAnotherPersonApprovesAndEveryStepIsLogged() throws Exception {
    String emp = login("fire_emp");
    String manager = login("fire_min");
    String guard = login("fire_guard");
    LocalDateTime end = LocalDateTime.now().minusHours(1);
    String apply = "{\"cameraId\":98401,\"rangeStart\":\"" + ISO.format(end.minusHours(2)) + "\",\"rangeEnd\":\"" + ISO.format(end) + "\",\"reason\":\"核查长者夜间跌倒经过\"}";
    mvc.perform(auth(post("/api/fire/video/access-requests"), guard).content(apply)).andExpect(status().isForbidden());
    String id = data(mvc.perform(auth(post("/api/fire/video/access-requests"), emp).content(apply)).andReturn().getResponse().getContentAsString()).path("id").asText();
    mvc.perform(auth(get("/api/fire/video/access-requests/" + id + "/recordings"), emp)).andExpect(status().isBadRequest());
    mvc.perform(auth(post("/api/fire/video/access-requests/" + id + "/decide"), emp).content("{\"approve\":true}")).andExpect(status().isForbidden());
    mvc.perform(auth(post("/api/fire/video/access-requests/" + id + "/decide"), manager).content("{\"approve\":true,\"note\":\"同意\"}"))
        .andExpect(jsonPath("$.data.status", is("APPROVED")));
    mvc.perform(auth(get("/api/fire/video/access-requests/" + id + "/recordings"), emp)).andExpect(status().isOk());
    mvc.perform(auth(get("/api/fire/video/access-requests/" + id + "/recordings"), manager))
        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message", containsString("申请人本人")));
    mvc.perform(auth(get("/api/fire/video/access-requests/" + id + "/logs"), manager))
        .andExpect(jsonPath("$.data[*].action").value(org.hamcrest.Matchers.contains("REQUEST", "APPROVE", "LIST")));
    mvc.perform(auth(get("/api/fire/video/live/98401"), emp)).andExpect(jsonPath("$.data.configured", is(false)));
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
