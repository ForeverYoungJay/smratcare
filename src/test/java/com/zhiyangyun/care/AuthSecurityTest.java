package com.zhiyangyun.care;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.model.LoginRequest;
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
class AuthSecurityTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void login_success() throws Exception {
    LoginRequest request = new LoginRequest();
    request.setUsername("admin");
    request.setPassword("123456");

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is(0)))
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
