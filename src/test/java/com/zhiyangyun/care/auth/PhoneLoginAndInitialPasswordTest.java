package com.zhiyangyun.care.auth;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "security.force-default-password-change=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PhoneLoginAndInitialPasswordTest {
  private static final String PHONE = "13700001234";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private StaffMapper staffMapper;
  @Autowired
  private PasswordEncoder passwordEncoder;

  @Test
  void phone_login_with_default_password_must_set_new_password_first() throws Exception {
    StaffAccount staff = new StaffAccount();
    staff.setId(990001L);
    staff.setOrgId(1L);
    staff.setDepartmentId(10L);
    staff.setStaffNo("S990001");
    staff.setUsername("phone_login_user");
    staff.setRealName("手机号登录");
    staff.setPhone(PHONE);
    staff.setPasswordHash(passwordEncoder.encode("123456"));
    staff.setStatus(1);
    staffMapper.insert(staff);

    // 手机号 + 默认密码：不给正式 token，只给改密令牌
    String first = login(PHONE, "123456");
    var firstData = objectMapper.readTree(first).path("data");
    org.junit.jupiter.api.Assertions.assertTrue(firstData.path("mustChangePassword").asBoolean());
    org.junit.jupiter.api.Assertions.assertTrue(firstData.path("token").isNull() || firstData.path("token").isMissingNode());
    String challenge = firstData.path("challengeToken").asText();

    // 改密令牌不能访问业务接口
    mockMvc.perform(get("/api/me/leave-balance").header("Authorization", "Bearer " + challenge))
        .andExpect(status().is(401));

    // 弱密码被拒绝
    mockMvc.perform(post("/api/auth/password/initial").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Map.of("challengeToken", challenge, "newPassword", "12345678"))))
        .andExpect(jsonPath("$.code", not(is(0))));

    // 设置新密码后直接登录成功
    mockMvc.perform(post("/api/auth/password/initial").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Map.of("challengeToken", challenge, "newPassword", "Abcd1234"))))
        .andExpect(jsonPath("$.code", is(0)))
        .andExpect(jsonPath("$.data.token", not(emptyOrNullString())));

    // 之后用手机号 + 新密码正常登录，旧默认密码失效
    var second = objectMapper.readTree(login(PHONE, "Abcd1234")).path("data");
    org.junit.jupiter.api.Assertions.assertFalse(second.path("token").asText().isEmpty());
    org.junit.jupiter.api.Assertions.assertFalse(second.path("mustChangePassword").asBoolean());
    org.junit.jupiter.api.Assertions.assertTrue(
        objectMapper.readTree(login(PHONE, "123456")).path("data").path("token").isMissingNode()
            || objectMapper.readTree(login(PHONE, "123456")).path("data").path("token").isNull());
  }

  private String login(String username, String password) throws Exception {
    return mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Map.of("username", username, "password", password))))
        .andReturn().getResponse().getContentAsString();
  }
}
