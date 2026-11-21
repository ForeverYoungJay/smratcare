package com.zhiyangyun.care.hr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import com.zhiyangyun.care.schedule.service.AttendanceService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
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

/** 人事：工资批次职责分离、考勤锁定与调整单、跨日夜班打卡、积分申诉与冲正、返聘查重与离职清单 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HrOpsFlowTest {
  private static final String HASH = "$2a$10$oHe4Uo7TeO5Ftwj0zK33auA8wXIMcbHScVrPTYbeTnrrrkbEJX.Qe";

  @Autowired private MockMvc mvc;
  @Autowired private ObjectMapper json;
  @Autowired private JdbcTemplate jdbc;
  @Autowired private AttendanceService attendance;

  @BeforeEach
  void seed() {
    String[][] users = {{"96101", "hrops_min", "人事部长", "96111", "HR_MINISTER", "13900000001"},
        {"96102", "hrops_emp", "人事专员", "96112", "HR_EMPLOYEE", "13900000002"},
        {"96103", "hrops_fin", "财务员", "96113", "FINANCE_EMPLOYEE", "13900000003"},
        {"96104", "hrops_nurse", "护理员", "96114", "NURSING_EMPLOYEE", "13900000004"}};
    for (String[] u : users) {
      jdbc.update("MERGE INTO staff (id, org_id, department_id, staff_no, username, password_hash, real_name, phone, status) KEY(id) VALUES (?, 1, 10, ?, ?, ?, ?, ?, 1)",
          Long.valueOf(u[0]), "H" + u[0], u[1], HASH, u[2], u[5]);
      jdbc.update("MERGE INTO role (id, org_id, role_name, role_code, status) KEY(id) VALUES (?, 1, ?, ?, 1)", Long.valueOf(u[3]), u[4], u[4]);
      jdbc.update("MERGE INTO staff_role (id, org_id, staff_id, role_id) KEY(id) VALUES (?, 1, ?, ?)", Long.valueOf(u[3]) + 100, Long.valueOf(u[0]), Long.valueOf(u[3]));
    }
  }

  @AfterEach
  void cleanup() {
    for (String table : new String[] {"hr_payroll_line", "hr_payroll_batch", "hr_attendance_period", "hr_attendance_adjustment",
        "hr_performance_appeal", "hr_staff_service_plan"}) {
      jdbc.update("DELETE FROM " + table + " WHERE org_id = 1");
    }
    jdbc.update("DELETE FROM attendance_record WHERE staff_id BETWEEN 96101 AND 96104");
    jdbc.update("DELETE FROM staff_points_log WHERE staff_id BETWEEN 96101 AND 96104");
    jdbc.update("DELETE FROM staff_points_account WHERE staff_id BETWEEN 96101 AND 96104");
  }

  @Test
  void payrollSeparatesPreparingApprovingAndPaying() throws Exception {
    String emp = login("hrops_emp");
    String min = login("hrops_min");
    String fin = login("hrops_fin");
    String nurse = login("hrops_nurse");
    String created = mvc.perform(auth(post("/api/admin/hr/payroll/batches"), emp).content("{\"period\":\"2026-10\"}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.data.status", is("DRAFT"))).andReturn().getResponse().getContentAsString();
    String batchId = data(created).path("id").asText();
    mvc.perform(auth(post("/api/admin/hr/payroll/batches"), emp).content("{\"period\":\"2026-10\"}")).andExpect(status().isBadRequest());
    Long lineId = jdbc.queryForObject("SELECT id FROM hr_payroll_line WHERE batch_id = ? AND staff_id = 96104", Long.class, Long.valueOf(batchId));
    mvc.perform(auth(put("/api/admin/hr/payroll/lines/" + lineId), emp)
        .content("{\"baseAmount\":5000.50,\"performanceAmount\":800,\"deductionAmount\":100.25,\"personalAmount\":500}"))
        .andExpect(jsonPath("$.data.grossAmount", is(5800.50))).andExpect(jsonPath("$.data.netAmount", is(5200.25)));
    mvc.perform(auth(put("/api/admin/hr/payroll/lines/" + lineId), emp).content("{\"baseAmount\":100,\"deductionAmount\":500}"))
        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message", containsString("负数")));

    mvc.perform(auth(post("/api/admin/hr/payroll/batches/" + batchId + "/submit"), emp)).andExpect(jsonPath("$.data.status", is("SUBMITTED")));
    mvc.perform(auth(post("/api/admin/hr/payroll/batches/" + batchId + "/approve"), emp)).andExpect(status().isForbidden());
    mvc.perform(auth(post("/api/admin/hr/payroll/batches/" + batchId + "/approve"), min)).andExpect(jsonPath("$.data.status", is("APPROVED")));
    mvc.perform(auth(put("/api/admin/hr/payroll/lines/" + lineId), emp).content("{\"baseAmount\":1}")).andExpect(status().isBadRequest());
    // 人事不能代替财务登记付款；财务必须先提交银行、再凭回执登记成功
    mvc.perform(auth(post("/api/admin/hr/payroll/batches/" + batchId + "/bank-submit"), min)).andExpect(status().isForbidden());
    mvc.perform(auth(post("/api/admin/hr/payroll/batches/" + batchId + "/mark-paid"), fin).content("{\"paymentRef\":\"B1\"}")).andExpect(status().isBadRequest());
    mvc.perform(auth(post("/api/admin/hr/payroll/batches/" + batchId + "/bank-submit"), fin)).andExpect(jsonPath("$.data.status", is("BANK_SUBMITTED")));
    mvc.perform(auth(post("/api/admin/hr/payroll/batches/" + batchId + "/mark-paid"), fin).content("{}")).andExpect(status().isBadRequest());
    mvc.perform(auth(post("/api/admin/hr/payroll/batches/" + batchId + "/mark-paid"), fin).content("{\"paymentRef\":\"BANK-20261105-01\"}"))
        .andExpect(jsonPath("$.data.status", is("PAID")));
    // 员工只看到自己的工资单，不能看批次明细
    mvc.perform(auth(get("/api/hr/payroll/my"), nurse))
        .andExpect(jsonPath("$.data.length()", is(1))).andExpect(jsonPath("$.data[0].line.netAmount", is(5200.25)));
    mvc.perform(auth(get("/api/admin/hr/payroll/batches/" + batchId + "/lines"), nurse)).andExpect(status().isForbidden());
  }

  @Test
  void lockedAttendanceChangesOnlyThroughApprovedAdjustment() throws Exception {
    String emp = login("hrops_emp");
    String min = login("hrops_min");
    YearMonth last = YearMonth.now().minusMonths(1);
    LocalDate day = last.atDay(10);
    jdbc.update("INSERT INTO attendance_record (id, org_id, staff_id, work_date, check_in_time, status) VALUES (96201, 1, 96104, ?, ?, 'MISSING_OUT')",
        day, day.atTime(8, 0));
    mvc.perform(auth(post("/api/admin/hr/attendance/periods/" + YearMonth.now() + "/lock"), min)).andExpect(status().isBadRequest());
    mvc.perform(auth(post("/api/admin/hr/attendance/periods/" + last + "/lock"), emp)).andExpect(status().isForbidden());
    mvc.perform(auth(post("/api/admin/hr/attendance/periods/" + last + "/lock"), min)).andExpect(status().isOk());
    assertThatThrownBy(() -> attendance.reviewRecord(1L, 96101L, 96201L, 1, "直接改")).hasMessageContaining("调整单");

    String requested = mvc.perform(auth(post("/api/admin/hr/attendance/adjustments"), emp)
        .content("{\"recordId\":96201,\"newCheckOut\":\"" + day + "T17:00:00\",\"newStatus\":\"NORMAL\",\"reason\":\"设备故障漏打卡，班组长确认\"}"))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    String adjustmentId = data(requested).path("id").asText();
    mvc.perform(auth(post("/api/admin/hr/attendance/adjustments/" + adjustmentId + "/decide"), emp).content("{\"approve\":true}")).andExpect(status().isForbidden());
    mvc.perform(auth(post("/api/admin/hr/attendance/adjustments/" + adjustmentId + "/decide"), min).content("{\"approve\":true,\"note\":\"同意\"}"))
        .andExpect(jsonPath("$.data.status", is("APPROVED")));
    assertThat(jdbc.queryForObject("SELECT status FROM attendance_record WHERE id = 96201", String.class)).isEqualTo("NORMAL");
  }

  @Test
  void overnightShiftClocksOutOnTheNextMorning() {
    LocalDateTime start = LocalDateTime.now().minusHours(8);
    jdbc.update("INSERT INTO attendance_record (id, org_id, staff_id, work_date, check_in_time, status) VALUES (96202, 1, 96104, ?, ?, 'ON_TIME')",
        LocalDate.now().minusDays(1), start);
    jdbc.update("UPDATE attendance_record SET work_date = ? WHERE id = 96202", LocalDate.now().minusDays(1));
    attendance.punch(1L, 96104L, "OUT");
    assertThat(jdbc.queryForObject("SELECT check_out_time FROM attendance_record WHERE id = 96202", LocalDateTime.class)).isNotNull();
  }

  @Test
  void acceptedPointsAppealReversesTheEntryOnce() throws Exception {
    String nurse = login("hrops_nurse");
    String min = login("hrops_min");
    jdbc.update("INSERT INTO staff_points_account (id, org_id, staff_id, points_balance, total_earned, total_deducted) VALUES (96301, 1, 96104, 50, 60, 10)");
    jdbc.update("INSERT INTO staff_points_log (id, org_id, staff_id, change_type, change_points, balance_after, source_type, remark) VALUES (96302, 1, 96104, 'DEDUCT', -10, 50, 'MANUAL', '迟到')");
    jdbc.update("INSERT INTO staff_points_log (id, org_id, staff_id, change_type, change_points, balance_after, source_type, remark) VALUES (96303, 1, 96101, 'EARN', 5, 5, 'MANUAL', 'x')");
    mvc.perform(auth(post("/api/hr/appeals"), nurse).content("{\"targetType\":\"POINTS_LOG\",\"targetId\":96303,\"reason\":\"不是我\"}"))
        .andExpect(status().isBadRequest());
    String appeal = mvc.perform(auth(post("/api/hr/appeals"), nurse).content("{\"targetType\":\"POINTS_LOG\",\"targetId\":96302,\"reason\":\"当天请假已批准\"}"))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    String appealId = data(appeal).path("id").asText();
    mvc.perform(auth(post("/api/admin/hr/appeals/" + appealId + "/decide"), min).content("{\"approve\":true,\"note\":\"核实请假记录属实\"}"))
        .andExpect(jsonPath("$.data.status", is("ACCEPTED")));
    assertThat(jdbc.queryForObject("SELECT points_balance FROM staff_points_account WHERE id = 96301", Integer.class)).isEqualTo(60);
    assertThat(jdbc.queryForObject("SELECT change_points FROM staff_points_log WHERE source_type = 'REVERSAL' AND source_id = 96302", Integer.class)).isEqualTo(10);
    // 原流水保留；不能重复冲正
    assertThat(jdbc.queryForObject("SELECT change_points FROM staff_points_log WHERE id = 96302", Integer.class)).isEqualTo(-10);
    mvc.perform(auth(post("/api/admin/hr/points/log/96302/reverse"), min).content("{\"reason\":\"再次\"}"))
        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message", containsString("已冲正")));
  }

  @Test
  void rehireIsFoundAndOffboardingListsOpenItems() throws Exception {
    String min = login("hrops_min");
    mvc.perform(auth(get("/api/admin/hr/staff/duplicate-check").param("phone", "13900000004"), min))
        .andExpect(jsonPath("$.data[0].staffId", is("96104"))).andExpect(jsonPath("$.data[0].samePhone", is(true)));
    jdbc.update("INSERT INTO hr_staff_service_plan (id, org_id, staff_id, live_in_dormitory, dormitory_building, dormitory_room_no, dormitory_bed_no) VALUES (96401, 1, 96104, 1, 'A栋', '302', '1')");
    mvc.perform(auth(get("/api/admin/hr/staff/96104/offboarding-checklist"), min))
        .andExpect(jsonPath("$.data[0].key", is("dormitory"))).andExpect(jsonPath("$.data[0].done", is(false)))
        .andExpect(jsonPath("$.data[3].done", is(false)));
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
