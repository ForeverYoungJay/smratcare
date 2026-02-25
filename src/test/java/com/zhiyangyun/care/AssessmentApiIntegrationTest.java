package com.zhiyangyun.care;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.assessment.entity.AssessmentRecord;
import com.zhiyangyun.care.assessment.mapper.AssessmentRecordMapper;
import com.zhiyangyun.care.auth.model.LoginRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
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
class AssessmentApiIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private AssessmentRecordMapper assessmentRecordMapper;

  @Test
  void score_preview_should_return_calculated_score_and_level() throws Exception {
    String token = loginAndGetToken("staff", "123456");

    Map<String, Object> body = new HashMap<>();
    body.put("templateId", 91001L);
    body.put("detailJson", "{\"q1\":2,\"q2\":4}");

    mockMvc.perform(post("/api/assessment/templates/score-preview")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is(0)))
        .andExpect(jsonPath("$.data.score", is(4.00)))
        .andExpect(jsonPath("$.data.levelCode", is("LOW")));
  }

  @Test
  void create_record_should_auto_score_by_template() throws Exception {
    String token = loginAndGetToken("staff", "123456");

    Map<String, Object> body = new HashMap<>();
    body.put("elderName", "Elder One");
    body.put("assessmentType", "SELF_CARE");
    body.put("templateId", 91001L);
    body.put("assessmentDate", LocalDate.now().toString());
    body.put("status", "COMPLETED");
    body.put("detailJson", "{\"q1\":2,\"q2\":4}");
    body.put("scoreAuto", 1);

    String response = mockMvc.perform(post("/api/assessment/records")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is(0)))
        .andExpect(jsonPath("$.data.score", is(4.00)))
        .andReturn()
        .getResponse()
        .getContentAsString();

    JsonNode node = objectMapper.readTree(response);
    Long id = node.path("data").path("id").asLong();
    AssessmentRecord saved = assessmentRecordMapper.selectById(id);
    org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("4.00"), saved.getScore());
    org.junit.jupiter.api.Assertions.assertEquals("LOW", saved.getLevelCode());
  }

  @Test
  void staff_cannot_access_record_of_other_org() throws Exception {
    AssessmentRecord record = new AssessmentRecord();
    record.setTenantId(2L);
    record.setOrgId(2L);
    record.setElderName("Org2 Elder");
    record.setAssessmentType("SELF_CARE");
    record.setAssessmentDate(LocalDate.now());
    record.setStatus("COMPLETED");
    record.setScore(new BigDecimal("10.00"));
    record.setScoreAuto(1);
    record.setIsDeleted(0);
    assessmentRecordMapper.insert(record);

    String token = loginAndGetToken("staff", "123456");

    mockMvc.perform(get("/api/assessment/records/" + record.getId())
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code", is(400)));
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

    JsonNode node = objectMapper.readTree(response);
    return node.path("data").path("token").asText();
  }
}
