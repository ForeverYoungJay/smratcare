package com.zhiyangyun.care.oa;

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
import java.time.LocalDate;
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

/** 我的办公：本人数据隔离（总结、考勤）、主管点评范围、修改密码、登录记录、本人档案 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MyOfficeFlowTest {
  private static final String HASH = "$2a$10$oHe4Uo7TeO5Ftwj0zK33auA8wXIMcbHScVrPTYbeTnrrrkbEJX.Qe";

  @Autowired private MockMvc mvc;
  @Autowired private ObjectMapper json;
  @Autowired private JdbcTemplate jdbc;

  @BeforeEach
  void seed() {
    // A、B 同部门员工，M 同部门部长，O 其他部门部长
    String[][] users = {{"95101", "me_a", "员工甲", "95111", "NURSING_EMPLOYEE", "10"},
        {"95102", "me_b", "员工乙", "95111", "NURSING_EMPLOYEE", "10"},
        {"95103", "me_m", "护理部长", "95113", "NURSING_MINISTER", "10"},
        {"95104", "me_o", "财务部长", "95114", "FINANCE_MINISTER", "20"}};
    for (String[] u : users) {
      jdbc.update("MERGE INTO staff (id, org_id, department_id, staff_no, username, password_hash, real_name, status) KEY(id) VALUES (?, 1, ?, ?, ?, ?, ?, 1)",
          Long.valueOf(u[0]), Long.valueOf(u[5]), "M" + u[0], u[1], HASH, u[2]);
      jdbc.update("MERGE INTO role (id, org_id, role_name, role_code, status) KEY(id) VALUES (?, 1, ?, ?, 1)", Long.valueOf(u[3]), u[4], u[4]);
      jdbc.update("MERGE INTO staff_role (id, org_id, staff_id, role_id) KEY(id) VALUES (?, 1, ?, ?)", Long.valueOf(u[0]) + 100, Long.valueOf(u[0]), Long.valueOf(u[3]));
    }
  }

  @AfterEach
  void cleanup() {
    jdbc.update("DELETE FROM oa_work_report WHERE reporter_id BETWEEN 95101 AND 95104");
    jdbc.update("DELETE FROM attendance_record WHERE staff_id BETWEEN 95101 AND 95104");
    jdbc.update("DELETE FROM auth_login_log WHERE staff_id BETWEEN 95101 AND 95104");
    jdbc.update("UPDATE staff SET password_hash = ? WHERE id = 95101", HASH);
  }

  @Test
  void reportsAreOwnForStaffDepartmentForMinistersAndLockedAfterReview() throws Exception {
    String a = login("me_a", "123456");
    String b = login("me_b", "123456");
    String m = login("me_m", "123456");
    String o = login("me_o", "123456");
    String created = mvc.perform(auth(post("/api/oa/report"), a)
        .content("{\"title\":\"第4周总结\",\"reportType\":\"WEEK\",\"completedWork\":\"巡房 36 次\"}"))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    String id = data(created).path("id").asText();

    // 员工乙即使传 mineOnly=false 也看不到同事的总结
    mvc.perform(auth(get("/api/oa/report/page").param("mineOnly", "false"), b)).andExpect(jsonPath("$.data.total", is("0")));
    // 本部门部长能看到，其他部门部长看不到
    mvc.perform(auth(get("/api/oa/report/page").param("mineOnly", "false"), m)).andExpect(jsonPath("$.data.total", is("1")));
    mvc.perform(auth(get("/api/oa/report/page").param("mineOnly", "false"), o)).andExpect(jsonPath("$.data.total", is("0")));

    mvc.perform(auth(put("/api/oa/report/" + id + "/review"), m).content("{\"comment\":\"不错\"}"))
        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message", containsString("已提交")));
    mvc.perform(auth(put("/api/oa/report/" + id + "/submit"), a)).andExpect(status().isOk());
    mvc.perform(auth(put("/api/oa/report/" + id + "/review"), b).content("{\"comment\":\"x\"}")).andExpect(status().isForbidden());
    mvc.perform(auth(put("/api/oa/report/" + id + "/review"), o).content("{\"comment\":\"x\"}")).andExpect(status().isBadRequest());
    mvc.perform(auth(put("/api/oa/report/" + id + "/review"), m).content("{\"comment\":\"执行到位，继续保持\",\"score\":4}"))
        .andExpect(jsonPath("$.data.reviewScore", is(4)));
    // 点评后员工不能再改
    mvc.perform(auth(put("/api/oa/report/" + id), a).content("{\"title\":\"改\",\"reportType\":\"WEEK\"}"))
        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message", containsString("点评")));
  }

  @Test
  void attendanceOverviewIgnoresAnotherStaffIdForOrdinaryStaff() throws Exception {
    String b = login("me_b", "123456");
    mvc.perform(auth(get("/api/attendance/overview").param("staffId", "95101"), b))
        .andExpect(jsonPath("$.data.staffId", is("95102")));
  }

  @Test
  void draftStatsCountOnlyMyOwnWork() throws Exception {
    String a = login("me_a", "123456");
    LocalDate today = LocalDate.now();
    jdbc.update("INSERT INTO attendance_record (id, org_id, staff_id, work_date, check_in_time, status) VALUES (95201, 1, 95101, ?, ?, 'NORMAL'), (95202, 1, 95102, ?, ?, 'NORMAL')",
        today, today.atTime(8, 0), today, today.atTime(8, 0));
    mvc.perform(auth(get("/api/oa/report/draft-stats").param("startDate", today.toString()).param("endDate", today.toString()), a))
        .andExpect(jsonPath("$.data.attendanceDays", is("1")));
  }

  @Test
  void staffChangeOwnPasswordAndSeeOwnLoginsAndProfile() throws Exception {
    String a = login("me_a", "123456");
    mvc.perform(auth(post("/api/me/password"), a).content("{\"oldPassword\":\"wrong\",\"newPassword\":\"Abc12345\"}"))
        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message", containsString("原密码")));
    mvc.perform(auth(post("/api/me/password"), a).content("{\"oldPassword\":\"123456\",\"newPassword\":\"short1\"}"))
        .andExpect(status().isBadRequest());
    mvc.perform(auth(post("/api/me/password"), a).content("{\"oldPassword\":\"123456\",\"newPassword\":\"Guifeng2026\"}"))
        .andExpect(status().isOk());
    assertThat(jdbc.queryForObject("SELECT password_plaintext_snapshot FROM staff WHERE id = 95101", String.class)).isNull();
    String again = login("me_a", "Guifeng2026");
    mvc.perform(auth(get("/api/me/logins"), again)).andExpect(jsonPath("$.data[0].result", containsString("SUCCESS")));
    login("me_b", "123456");
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM auth_login_log WHERE staff_id = 95101", Integer.class))
        .isEqualTo(json.readTree(mvc.perform(auth(get("/api/me/logins"), again)).andReturn().getResponse().getContentAsString()).path("data").size());
    mvc.perform(auth(get("/api/me/profile"), again)).andExpect(jsonPath("$.data.staffNo", is("M95101")));
  }

  private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder builder, String token) {
    return builder.header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON);
  }

  private JsonNode data(String response) throws Exception {
    return json.readTree(response).path("data");
  }

  private String login(String username, String password) throws Exception {
    LoginRequest request = new LoginRequest();
    request.setUsername(username);
    request.setPassword(password);
    String response = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request)))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    return json.readTree(response).path("data").path("token").asText();
  }
}
