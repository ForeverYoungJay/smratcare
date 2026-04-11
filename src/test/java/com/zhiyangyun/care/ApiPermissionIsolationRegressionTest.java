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
import com.zhiyangyun.care.store.entity.Product;
import com.zhiyangyun.care.store.mapper.ProductMapper;
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

  @Autowired
  private ProductMapper productMapper;

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

  @Test
  void finance_minister_should_not_query_billing_of_other_org() throws Exception {
    String username = "finance_scope_" + ID_SEQ.incrementAndGet();
    createStaffWithRole(username, "FINANCE_MINISTER", 1L);
    String token = loginAndGetToken(username, "123456");

    mockMvc.perform(get("/api/admin/billing/config")
            .header("Authorization", "Bearer " + token)
            .param("orgId", "2"))
        .andExpect(status().isForbidden());
  }

  @Test
  void logistics_staff_should_not_read_product_from_other_org() throws Exception {
    Product product = createProduct(2L, "跨机构商品");
    String username = "logistics_scope_" + ID_SEQ.incrementAndGet();
    createStaffWithRole(username, "LOGISTICS_EMPLOYEE", 1L);
    String token = loginAndGetToken(username, "123456");

    mockMvc.perform(get("/api/store/product/" + product.getId())
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }

  @Test
  void admin_should_not_assign_role_from_other_org_to_target_staff() throws Exception {
    Long targetStaffId = createStaffWithRole("org2_target_" + ID_SEQ.incrementAndGet(), "HR_EMPLOYEE", 2L);
    Long org1RoleId = ensureRole("MARKETING_EMPLOYEE", 1L);
    String adminToken = loginAndGetToken("admin", "123456");

    mockMvc.perform(post("/api/admin/staff-roles/assign")
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "staffId": %d,
                  "roleIds": [%d]
                }
                """.formatted(targetStaffId, org1RoleId)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code", is(400)));
  }

  @Test
  void role_update_should_keep_existing_role_code_when_request_omits_it() throws Exception {
    ensureRole("NURSING_MINISTER", 1L);

    Role customRole = new Role();
    customRole.setId(ID_SEQ.incrementAndGet());
    customRole.setOrgId(1L);
    customRole.setDepartmentId(10L);
    customRole.setRoleCode("CUSTOM_CARE_LEAD");
    customRole.setRoleName("照护组长");
    customRole.setRoleDesc("custom role");
    customRole.setStatus(1);
    roleMapper.insert(customRole);

    String adminToken = loginAndGetToken("admin", "123456");

    mockMvc.perform(put("/api/admin/roles/" + customRole.getId())
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "roleName": "护理部部长",
                  "departmentId": 10,
                  "status": 1
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is(0)))
        .andExpect(jsonPath("$.data.roleCode", is("CUSTOM_CARE_LEAD")))
        .andExpect(jsonPath("$.data.roleName", is("护理部部长")));

    Role updated = roleMapper.selectById(customRole.getId());
    org.junit.jupiter.api.Assertions.assertEquals("CUSTOM_CARE_LEAD", updated.getRoleCode());
    org.junit.jupiter.api.Assertions.assertEquals("护理部部长", updated.getRoleName());
  }

  private Long createStaffWithRole(String username, String roleCode) {
    return createStaffWithRole(username, roleCode, 1L);
  }

  private Long createStaffWithRole(String username, String roleCode, Long orgId) {
    Long roleId = ensureRole(roleCode, orgId);
    Long staffId = ID_SEQ.incrementAndGet();
    StaffAccount staff = new StaffAccount();
    staff.setId(staffId);
    staff.setOrgId(orgId);
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
    staffRole.setOrgId(orgId);
    staffRole.setStaffId(staffId);
    staffRole.setRoleId(roleId);
    staffRoleMapper.insert(staffRole);
    return staffId;
  }

  private Long ensureRole(String roleCode) {
    return ensureRole(roleCode, 1L);
  }

  private Long ensureRole(String roleCode, Long orgId) {
    Role exists = roleMapper.selectOne(com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(Role.class)
        .eq(Role::getOrgId, orgId)
        .eq(Role::getRoleCode, roleCode)
        .eq(Role::getIsDeleted, 0)
        .last("LIMIT 1"));
    if (exists != null) return exists.getId();

    Role role = new Role();
    role.setId(ID_SEQ.incrementAndGet());
    role.setOrgId(orgId);
    role.setRoleCode(roleCode);
    role.setRoleName(roleCode);
    role.setRoleDesc("test role " + roleCode);
    role.setStatus(1);
    roleMapper.insert(role);
    return role.getId();
  }

  private Product createProduct(Long orgId, String productName) {
    Product product = new Product();
    product.setId(ID_SEQ.incrementAndGet());
    product.setOrgId(orgId);
    product.setProductCode("P" + product.getId());
    product.setProductName(productName);
    product.setCategory("SUPPLY");
    product.setBusinessDomain("STORE");
    product.setItemType("CONSUMABLE");
    product.setMallEnabled(1);
    product.setPrice(java.math.BigDecimal.TEN);
    product.setPointsPrice(0);
    product.setSafetyStock(0);
    product.setStatus(1);
    product.setIsDeleted(0);
    productMapper.insert(product);
    return product;
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
