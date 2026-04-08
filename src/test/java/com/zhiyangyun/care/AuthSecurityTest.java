package com.zhiyangyun.care;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.model.FamilyLoginRequest;
import com.zhiyangyun.care.auth.model.LoginRequest;
import com.zhiyangyun.care.elder.entity.FamilyUser;
import com.zhiyangyun.care.elder.mapper.FamilyUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthSecurityTest {
  private static final String TEST_FAMILY_PHONE = "13000000012";
  private static final String TEST_FAMILY_PASSWORD = "Family@123";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private FamilyUserMapper familyUserMapper;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Test
  void login_success() throws Exception {
    LoginRequest request = new LoginRequest();
    request.setUsername("admin");
    request.setPassword("123456");

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(header().exists("X-Request-Id"))
        .andExpect(jsonPath("$.code", is(0)))
        .andExpect(jsonPath("$.success", is(true)))
        .andExpect(jsonPath("$.timestamp").isNumber())
        .andExpect(jsonPath("$.requestId").isString())
        .andExpect(jsonPath("$.data.token").exists());
  }

  @Test
  void login_fail() throws Exception {
    LoginRequest request = new LoginRequest();
    request.setUsername("admin");
    request.setPassword("wrong");

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void admin_can_access_admin_api() throws Exception {
    String token = loginAndGetToken("admin", "123456");

    mockMvc.perform(get("/api/admin/orgs")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is(0)));
  }

  @Test
  void admin_can_access_guard_api() throws Exception {
    String token = loginAndGetToken("admin", "123456");

    mockMvc.perform(get("/api/guard/visit/today")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
  }

  @Test
  void staff_cannot_access_admin_api() throws Exception {
    String token = loginAndGetToken("staff", "123456");

    mockMvc.perform(get("/api/admin/orgs")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }

  @Test
  void staff_cannot_access_billing_admin_api() throws Exception {
    String token = loginAndGetToken("staff", "123456");

    mockMvc.perform(get("/api/admin/billing/config")
            .param("orgId", "1")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }

  @Test
  void staff_cannot_access_health_module_without_medical_or_nursing_role() throws Exception {
    String token = loginAndGetToken("staff", "123456");

    mockMvc.perform(get("/api/health/archive/page")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }

  @Test
  void family_cannot_access_staff_stats_api() throws Exception {
    String token = loginAndGetFamilyToken();

    mockMvc.perform(get("/api/stats/check-in")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }

  @Test
  void family_can_upload_file_after_login() throws Exception {
    String token = loginAndGetFamilyToken();
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "voice.txt",
        MediaType.TEXT_PLAIN_VALUE,
        "voice".getBytes());

    mockMvc.perform(multipart("/api/files/upload")
            .file(file)
            .param("bizType", "family-voice")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is(0)))
        .andExpect(jsonPath("$.data.fileUrl").exists());
  }

  @Test
  void family_upload_rejects_dangerous_content_type() throws Exception {
    String token = loginAndGetFamilyToken();
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "voice.txt",
        MediaType.TEXT_HTML_VALUE,
        "<script>alert(1)</script>".getBytes());

    mockMvc.perform(multipart("/api/files/upload")
            .file(file)
            .param("bizType", "family-voice")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code", is(400)))
        .andExpect(jsonPath("$.success", is(false)))
        .andExpect(jsonPath("$.message", is("文件内容类型不支持")));
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

  private String loginAndGetFamilyToken() throws Exception {
    ensureFamilyUser();

    FamilyLoginRequest request = new FamilyLoginRequest();
    request.setOrgId(1L);
    request.setPhone(TEST_FAMILY_PHONE);
    request.setPassword(TEST_FAMILY_PASSWORD);

    String response = mockMvc.perform(post("/api/auth/family/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    return objectMapper.readTree(response).path("data").path("token").asText();
  }

  private void ensureFamilyUser() {
    FamilyUser user = familyUserMapper.selectOne(
        com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(FamilyUser.class)
            .eq(FamilyUser::getOrgId, 1L)
            .eq(FamilyUser::getPhone, TEST_FAMILY_PHONE)
            .eq(FamilyUser::getIsDeleted, 0)
            .last("LIMIT 1"));
    if (user == null) {
      FamilyUser created = new FamilyUser();
      created.setOrgId(1L);
      created.setUsername(TEST_FAMILY_PHONE);
      created.setPhone(TEST_FAMILY_PHONE);
      created.setRealName("权限测试家属");
      created.setStatus(1);
      created.setPasswordHash(passwordEncoder.encode(TEST_FAMILY_PASSWORD));
      familyUserMapper.insert(created);
      return;
    }
    user.setUsername(TEST_FAMILY_PHONE);
    user.setStatus(1);
    user.setPasswordHash(passwordEncoder.encode(TEST_FAMILY_PASSWORD));
    familyUserMapper.updateById(user);
  }
}
