package com.zhiyangyun.care;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.model.FamilyLoginRequest;
import com.zhiyangyun.care.visit.model.VisitBookRequest;
import java.time.LocalDateTime;
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
class FamilyAuthTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void family_login_success() throws Exception {
    FamilyLoginRequest request = new FamilyLoginRequest();
    request.setOrgId(1L);
    request.setPhone("13000000002");
    request.setVerifyCode("0000");

    mockMvc.perform(post("/api/auth/family/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is(0)))
        .andExpect(jsonPath("$.data.token").exists());
  }

  @Test
  void family_requires_auth() throws Exception {
    VisitBookRequest request = new VisitBookRequest();
    request.setOrgId(1L);
    request.setElderId(200L);
    request.setFamilyUserId(1L);
    request.setVisitTime(LocalDateTime.now().plusHours(1));
    request.setVisitTimeSlot("AM");

    mockMvc.perform(post("/api/family/visit/book")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }
}
