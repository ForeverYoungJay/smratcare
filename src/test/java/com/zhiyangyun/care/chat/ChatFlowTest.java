package com.zhiyangyun.care.chat;

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

/** 协同消息：会话访问只看成员身份、发送幂等、已读游标、消息转待办（统一任务、防重复、不外泄群聊）；待办转交催办；假期余额 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChatFlowTest {
  private static final String HASH = "$2a$10$oHe4Uo7TeO5Ftwj0zK33auA8wXIMcbHScVrPTYbeTnrrrkbEJX.Qe";

  @Autowired private MockMvc mvc;
  @Autowired private ObjectMapper json;
  @Autowired private JdbcTemplate jdbc;

  @BeforeEach
  void seed() {
    // 王护士、李工、外人张三、行政部长
    String[][] users = {{"94101", "ch_nurse", "王护士", "94111", "NURSING_EMPLOYEE"},
        {"94102", "ch_fix", "李工", "94112", "LOGISTICS_EMPLOYEE"},
        {"94103", "ch_out", "张三", "94111", "NURSING_EMPLOYEE"},
        {"94104", "ch_hr", "行政部长", "94114", "HR_MINISTER"}};
    for (String[] u : users) {
      jdbc.update("MERGE INTO staff (id, org_id, department_id, staff_no, username, password_hash, real_name, status) KEY(id) VALUES (?, 1, 10, ?, ?, ?, ?, 1)",
          Long.valueOf(u[0]), "C" + u[0], u[1], HASH, u[2]);
      jdbc.update("MERGE INTO role (id, org_id, role_name, role_code, status) KEY(id) VALUES (?, 1, ?, ?, 1)", Long.valueOf(u[3]), u[4], u[4]);
      jdbc.update("MERGE INTO staff_role (id, org_id, staff_id, role_id) KEY(id) VALUES (?, 1, ?, ?)", Long.valueOf(u[0]) + 100, Long.valueOf(u[0]), Long.valueOf(u[3]));
    }
  }

  @AfterEach
  void cleanup() {
    jdbc.update("DELETE FROM chat_message WHERE conversation_id IN (SELECT conversation_id FROM chat_member WHERE staff_id BETWEEN 94101 AND 94104)");
    jdbc.update("DELETE FROM chat_task_link WHERE created_by BETWEEN 94101 AND 94104");
    jdbc.update("DELETE FROM chat_conversation WHERE created_by BETWEEN 94101 AND 94104");
    jdbc.update("DELETE FROM chat_member WHERE staff_id BETWEEN 94101 AND 94104");
    jdbc.update("DELETE FROM oa_task WHERE created_by BETWEEN 94101 AND 94104");
    jdbc.update("DELETE FROM oa_todo WHERE created_by BETWEEN 94101 AND 94104");
    jdbc.update("DELETE FROM oa_approval WHERE applicant_id BETWEEN 94101 AND 94104");
    jdbc.update("DELETE FROM hr_leave_quota WHERE staff_id BETWEEN 94101 AND 94104");
  }

  @Test
  void groupChatIsMembersOnlyIdempotentAndTracksReadCursor() throws Exception {
    String nurse = login("ch_nurse");
    String fix = login("ch_fix");
    String outsider = login("ch_out");
    String convId = data(mvc.perform(auth(post("/api/chat/conversations"), nurse)
        .content("{\"type\":\"GROUP\",\"title\":\"护理后勤协同群\",\"memberIds\":[94102]}"))
        .andExpect(status().isOk()).andReturn()).path("id").asText();
    // 新群只有系统消息、没有任务时也要能拉取（曾因 Map.of().get(null) 500）
    mvc.perform(auth(get("/api/chat/conversations/" + convId + "/messages"), nurse)).andExpect(status().isOk());

    String body = "{\"body\":\"A栋302排水不畅\",\"clientMessageId\":\"c-1\"}";
    String first = data(mvc.perform(auth(post("/api/chat/conversations/" + convId + "/messages"), nurse).content(body)).andReturn()).path("id").asText();
    String retry = data(mvc.perform(auth(post("/api/chat/conversations/" + convId + "/messages"), nurse).content(body)).andReturn()).path("id").asText();
    assertThat(retry).isEqualTo(first);

    // 非成员读不到消息、成员、也发不了消息
    mvc.perform(auth(get("/api/chat/conversations/" + convId + "/messages"), outsider)).andExpect(status().isForbidden());
    mvc.perform(auth(get("/api/chat/conversations/" + convId + "/members"), outsider)).andExpect(status().isForbidden());
    mvc.perform(auth(post("/api/chat/conversations/" + convId + "/messages"), outsider).content("{\"body\":\"x\"}")).andExpect(status().isForbidden());
    mvc.perform(auth(get("/api/chat/search").param("q", "排水"), outsider)).andExpect(jsonPath("$.data.length()", is(0)));

    // 李工未读 1 条（系统消息+正文中只有他人发的算未读），读到最新后清零
    JsonNode list = data(mvc.perform(auth(get("/api/chat/conversations"), fix)).andReturn());
    assertThat(list.get(0).path("unread").asLong()).isGreaterThan(0);
    mvc.perform(auth(post("/api/chat/conversations/" + convId + "/read"), fix).content("{\"messageId\":" + first + "}")).andExpect(status().isOk());
    mvc.perform(auth(get("/api/chat/conversations"), fix)).andExpect(jsonPath("$.data[0].unread", is("0")));

    // 群主移出李工后他立即失去访问权
    mvc.perform(auth(post("/api/chat/conversations/" + convId + "/members"), fix).content("{\"remove\":[94101]}")).andExpect(status().isForbidden());
    mvc.perform(auth(post("/api/chat/conversations/" + convId + "/members"), nurse).content("{\"remove\":[94102]}")).andExpect(status().isOk());
    mvc.perform(auth(get("/api/chat/conversations/" + convId + "/messages"), fix)).andExpect(status().isForbidden());
  }

  @Test
  void messageBecomesOneUnifiedTaskWithoutLeakingTheChat() throws Exception {
    String nurse = login("ch_nurse");
    String hr = login("ch_hr");
    String convId = data(mvc.perform(auth(post("/api/chat/conversations"), nurse)
        .content("{\"type\":\"GROUP\",\"title\":\"护理群\",\"memberIds\":[94103]}")).andReturn()).path("id").asText();
    String msgId = data(mvc.perform(auth(post("/api/chat/conversations/" + convId + "/messages"), nurse)
        .content("{\"body\":\"302卫生间积水\",\"clientMessageId\":\"c-2\"}")).andReturn()).path("id").asText();

    String convert = "{\"title\":\"A栋302排水维修\",\"assigneeId\":94102,\"priority\":\"HIGH\"}";
    JsonNode created = data(mvc.perform(auth(post("/api/chat/messages/" + msgId + "/convert-to-task"), nurse).content(convert)).andReturn());
    String taskId = created.path("taskId").asText();
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM oa_task WHERE id = ? AND assignee_id = 94102 AND status = 'OPEN'", Integer.class, Long.valueOf(taskId))).isEqualTo(1);

    // 重复点击不会再建一张，除非明确确认
    mvc.perform(auth(post("/api/chat/messages/" + msgId + "/convert-to-task"), nurse).content(convert))
        .andExpect(jsonPath("$.data.duplicate", is(true))).andExpect(jsonPath("$.data.existingTaskIds[0]", is(taskId)));
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM chat_task_link WHERE message_id = ?", Integer.class, Long.valueOf(msgId))).isEqualTo(1);

    // 负责人在「聊天待办」看到任务，但看不到来源会话；也读不到群消息
    String fix = login("ch_fix");
    mvc.perform(auth(get("/api/chat/tasks").param("view", "mine"), fix))
        .andExpect(jsonPath("$.data[0].taskId", is(taskId))).andExpect(jsonPath("$.data[0].conversationId").doesNotExist());
    mvc.perform(auth(get("/api/chat/conversations/" + convId + "/messages"), fix)).andExpect(status().isForbidden());
    // 无关人员看不到这个任务
    mvc.perform(auth(get("/api/chat/tasks"), hr)).andExpect(jsonPath("$.data.length()", is(0)));
    // 任务卡写回了会话，状态来自统一任务
    mvc.perform(auth(get("/api/chat/conversations/" + convId + "/messages"), nurse))
        .andExpect(jsonPath("$.data[?(@.type == 'TASK_CARD')].task.status").value(org.hamcrest.Matchers.hasItem("OPEN")));
  }

  @Test
  void todoTransferUrgeAndHistoryAreAuditedAndScoped() throws Exception {
    String nurse = login("ch_nurse");
    String outsider = login("ch_out");
    jdbc.update("INSERT INTO oa_todo (id, org_id, title, status, assignee_id, assignee_name, created_by, is_deleted) VALUES (94201, 1, '补护理记录', 'OPEN', 94101, '王护士', 94101, 0)");
    mvc.perform(auth(put("/api/oa/todo/94201/transfer"), outsider).content("{\"staffId\":94103}")).andExpect(status().isBadRequest());
    mvc.perform(auth(put("/api/oa/todo/94201/transfer"), nurse).content("{\"staffId\":94103,\"note\":\"今天休息\"}"))
        .andExpect(jsonPath("$.data.assigneeName", is("张三")));
    mvc.perform(auth(post("/api/oa/todo/94201/urge"), nurse)).andExpect(status().isOk());
    mvc.perform(auth(post("/api/oa/todo/94201/urge"), nurse)).andExpect(jsonPath("$.message", containsString("30 分钟")));
    mvc.perform(auth(get("/api/oa/todo/94201/history"), outsider)).andExpect(jsonPath("$.data.length()", is(2)));
  }

  @Test
  void leaveBalanceSubtractsApprovedLeaveOfTheSameType() throws Exception {
    String hr = login("ch_hr");
    int year = LocalDate.now().getYear();
    mvc.perform(auth(post("/api/admin/hr/leave-quota"), hr).content("{\"staffId\":94101,\"year\":" + year + ",\"leaveType\":\"ANNUAL\",\"quotaDays\":10}"))
        .andExpect(status().isOk());
    jdbc.update("INSERT INTO oa_approval (id, org_id, approval_type, title, applicant_id, applicant_name, start_time, form_data, status, is_deleted) VALUES "
        + "(94301, 1, 'LEAVE', '年假', 94101, '王护士', ?, '{\"leaveType\":\"ANNUAL\",\"leaveDays\":2.5}', 'APPROVED', 0),"
        + "(94302, 1, 'LEAVE', '年假', 94101, '王护士', ?, '{\"leaveType\":\"ANNUAL\",\"leaveDays\":3}', 'PENDING', 0)",
        LocalDate.of(year, 3, 1).atStartOfDay(), LocalDate.of(year, 4, 1).atStartOfDay());
    mvc.perform(auth(get("/api/me/leave-balance"), login("ch_nurse")))
        .andExpect(jsonPath("$.data[0].remainingDays", is(7.5)));
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
