package com.zhiyangyun.care;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.entity.Role;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.entity.StaffRole;
import com.zhiyangyun.care.auth.mapper.RoleMapper;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.auth.mapper.StaffRoleMapper;
import com.zhiyangyun.care.auth.model.LoginRequest;
import com.zhiyangyun.care.oa.entity.OaTodo;
import com.zhiyangyun.care.oa.mapper.OaTodoMapper;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiPermissionIsolationRegressionTest {

  private static final AtomicLong ID_SEQ = new AtomicLong(980000);
  private static final String DEFAULT_PASSWORD_HASH = "$2a$10$oHe4Uo7TeO5Ftwj0zK33auA8wXIMcbHScVrPTYbeTnrrrkbEJX.Qe";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private RoleMapper roleMapper;

  @Autowired
  private StaffMapper staffMapper;

  @Autowired
  private StaffRoleMapper staffRoleMapper;

  @Autowired
  private OaTodoMapper todoMapper;

  @Test
  void todo_page_should_isolate_records_for_employee() throws Exception {
    Long peerStaffId = createStaffWithRole("todo_peer_" + ID_SEQ.incrementAndGet(), "STAFF");

    OaTodo peerTodo = new OaTodo();
    peerTodo.setOrgId(1L);
    peerTodo.setTenantId(1L);
    peerTodo.setTitle("peer-only-todo");
    peerTodo.setContent("should-not-be-visible");
    peerTodo.setStatus("OPEN");
    peerTodo.setAssigneeId(peerStaffId);
    peerTodo.setAssigneeName("Peer");
    peerTodo.setCreatedBy(peerStaffId);
    peerTodo.setDueTime(LocalDateTime.now().plusDays(1));
    todoMapper.insert(peerTodo);

    String staffToken = loginAndGetToken("staff", "123456");

    mockMvc.perform(post("/api/oa/todo")
            .header("Authorization", "Bearer " + staffToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "title":"self-todo",
                  "content":"employee own todo",
                  "status":"OPEN"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is(0)));

    String pageResp = mockMvc.perform(get("/api/oa/todo/page")
            .header("Authorization", "Bearer " + staffToken)
            .param("pageNo", "1")
            .param("pageSize", "50"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is(0)))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JsonNode records = objectMapper.readTree(pageResp).path("data").path("records");
    org.junit.jupiter.api.Assertions.assertTrue(records.isArray());
    org.junit.jupiter.api.Assertions.assertTrue(records.size() > 0);
    for (JsonNode node : records) {
      String title = node.path("title").asText("");
      org.junit.jupiter.api.Assertions.assertNotEquals("peer-only-todo", title, "员工不应看到他人的待办");
    }
  }

  @Test
  void approval_should_forbid_employee_approve_but_allow_minister() throws Exception {
    createStaffWithRole("finance_emp_" + ID_SEQ.incrementAndGet(), "FINANCE_EMPLOYEE");
    String financeMinisterUsername = "finance_minister_" + ID_SEQ.incrementAndGet();
    Long financeMinisterId = createStaffWithRole(financeMinisterUsername, "FINANCE_MINISTER");
    org.junit.jupiter.api.Assertions.assertNotNull(financeMinisterId);

    String employeeToken = loginAndGetToken("staff", "123456");

    Map<String, Object> createBody = new HashMap<>();
    createBody.put("approvalType", "REIMBURSE");
    createBody.put("title", "回归测试-财务审批");
    createBody.put("amount", 200);
    createBody.put("status", "PENDING");
    createBody.put("formData", "{\"policyAcknowledged\":true}");

    String createResp = mockMvc.perform(post("/api/oa/approval")
            .header("Authorization", "Bearer " + employeeToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createBody)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is(0)))
        .andReturn()
        .getResponse()
        .getContentAsString();

    Long approvalId = objectMapper.readTree(createResp).path("data").path("id").asLong();
    org.junit.jupiter.api.Assertions.assertTrue(approvalId != null && approvalId > 0);

    mockMvc.perform(put("/api/oa/approval/" + approvalId + "/approve")
            .header("Authorization", "Bearer " + employeeToken))
        .andExpect(status().isForbidden());

    String ministerToken = loginAndGetToken(financeMinisterUsername, "123456");

    mockMvc.perform(put("/api/oa/approval/" + approvalId + "/approve")
            .header("Authorization", "Bearer " + ministerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is(0)));
  }

  @Test
  void crm_lead_page_should_isolate_records_for_employee() throws Exception {
    String ownerA = "marketing_emp_" + ID_SEQ.incrementAndGet();
    String ownerB = "marketing_emp_" + ID_SEQ.incrementAndGet();
    createStaffWithRole(ownerA, "MARKETING_EMPLOYEE");
    createStaffWithRole(ownerB, "MARKETING_EMPLOYEE");

    String tokenA = loginAndGetToken(ownerA, "123456");
    String tokenB = loginAndGetToken(ownerB, "123456");

    Map<String, Object> leadA = new HashMap<>();
    leadA.put("name", "线索-A");
    leadA.put("phone", "13900000001");
    leadA.put("source", "抖音");
    leadA.put("status", 0);
    leadA.put("consultDate", "2026-03-08");

    Map<String, Object> leadB = new HashMap<>();
    leadB.put("name", "线索-B");
    leadB.put("phone", "13900000002");
    leadB.put("source", "微信");
    leadB.put("status", 0);
    leadB.put("consultDate", "2026-03-08");

    mockMvc.perform(post("/api/crm/leads")
            .header("Authorization", "Bearer " + tokenA)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(leadA)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is(0)));

    mockMvc.perform(post("/api/crm/leads")
            .header("Authorization", "Bearer " + tokenB)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(leadB)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is(0)));

    String pageRespA = mockMvc.perform(get("/api/crm/leads/page")
            .header("Authorization", "Bearer " + tokenA)
            .param("pageNo", "1")
            .param("pageSize", "50"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is(0)))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JsonNode recordsA = objectMapper.readTree(pageRespA).path("data").path("records");
    org.junit.jupiter.api.Assertions.assertTrue(recordsA.isArray());
    org.junit.jupiter.api.Assertions.assertTrue(recordsA.size() > 0);
    for (JsonNode node : recordsA) {
      String name = node.path("name").asText("");
      org.junit.jupiter.api.Assertions.assertNotEquals("线索-B", name, "员工不应看到他人创建的市场线索");
    }
  }

  private Long createStaffWithRole(String username, String roleCode) {
    Long roleId = ensureRole(roleCode);
    Long staffId = ID_SEQ.incrementAndGet();
    StaffAccount staff = new StaffAccount();
    staff.setId(staffId);
    staff.setOrgId(1L);
    staff.setDepartmentId(10L);
    staff.setStaffNo("S" + staffId);
    staff.setUsername(username);
    staff.setPasswordHash(DEFAULT_PASSWORD_HASH);
    staff.setRealName(username);
    staff.setPhone("139" + String.format("%08d", staffId % 100000000));
    staff.setStatus(1);
    staffMapper.insert(staff);

    StaffRole staffRole = new StaffRole();
    staffRole.setId(ID_SEQ.incrementAndGet());
    staffRole.setOrgId(1L);
    staffRole.setStaffId(staffId);
    staffRole.setRoleId(roleId);
    staffRoleMapper.insert(staffRole);
    return staffId;
  }

  private Long ensureRole(String roleCode) {
    Role exists = roleMapper.selectOne(com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(Role.class)
        .eq(Role::getOrgId, 1L)
        .eq(Role::getRoleCode, roleCode)
        .eq(Role::getIsDeleted, 0)
        .last("LIMIT 1"));
    if (exists != null) return exists.getId();

    Role role = new Role();
    role.setId(ID_SEQ.incrementAndGet());
    role.setOrgId(1L);
    role.setRoleCode(roleCode);
    role.setRoleName(roleCode);
    role.setRoleDesc("test role " + roleCode);
    role.setStatus(1);
    roleMapper.insert(role);
    return role.getId();
  }

  private String loginAndGetToken(String username, String password) throws Exception {
    LoginRequest request = new LoginRequest();
    request.setUsername(username);
    request.setPassword(password);

    String response = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    return objectMapper.readTree(response).path("data").path("token").asText();
  }
}
