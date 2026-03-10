package com.zhiyangyun.care;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.elder.entity.FamilyUser;
import com.zhiyangyun.care.elder.mapper.FamilyUserMapper;
import com.zhiyangyun.care.auth.model.FamilyLoginRequest;
import com.zhiyangyun.care.visit.model.VisitBookRequest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FamilyAuthTest {
  private static final String TEST_PHONE = "13000000002";
  private static final String TEST_PASSWORD = "Family@123";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private FamilyUserMapper familyUserMapper;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Test
  void family_login_success() throws Exception {
    ensureFamilyUser();

    FamilyLoginRequest request = new FamilyLoginRequest();
    request.setOrgId(1L);
    request.setPhone(TEST_PHONE);
    request.setPassword(TEST_PASSWORD);

    mockMvc.perform(post("/api/auth/family/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is(0)))
        .andExpect(jsonPath("$.data.token").exists());
  }

  private void ensureFamilyUser() {
    FamilyUser user = familyUserMapper.selectOne(
        Wrappers.lambdaQuery(FamilyUser.class)
            .eq(FamilyUser::getOrgId, 1L)
            .eq(FamilyUser::getPhone, TEST_PHONE)
            .eq(FamilyUser::getIsDeleted, 0)
            .last("LIMIT 1"));
    if (user == null) {
      FamilyUser created = new FamilyUser();
      created.setOrgId(1L);
      created.setUsername(TEST_PHONE);
      created.setPhone(TEST_PHONE);
      created.setRealName("测试家属");
      created.setStatus(1);
      created.setPasswordHash(passwordEncoder.encode(TEST_PASSWORD));
      familyUserMapper.insert(created);
      return;
    }
    user.setUsername(TEST_PHONE);
    user.setStatus(1);
    user.setPasswordHash(passwordEncoder.encode(TEST_PASSWORD));
    familyUserMapper.updateById(user);
  }

  @Test
  void family_requires_auth() throws Exception {
    VisitBookRequest request = new VisitBookRequest();
    request.setOrgId(1L);
    request.setElderId(200L);
    request.setFamilyUserId(1L);
    request.setVisitTime(LocalDateTime.now().plusHours(1));
    request.setVisitTimeSlot("AM");

    mockMvc.perform(post("/api/family/visit/video/book")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }
}
