package com.zhiyangyun.care;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.oa.entity.OaApproval;
import com.zhiyangyun.care.oa.mapper.OaApprovalMapper;
import java.util.List;
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
class OutboundSheetApprovalTest {
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private OaApprovalMapper oaApprovalMapper;

  @Test
  void sheet_cannot_be_confirmed_until_material_apply_is_approved() throws Exception {
    String token = login();
    Map<String, Object> body = Map.of("receiverName", "护理部", "applyDept", "护理部",
        "items", List.of(Map.of("productId", 6003, "quantity", 1, "reason", "领用")));
    String created = mockMvc.perform(post("/api/inventory/outbound/sheet")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)))
        .andExpect(jsonPath("$.code", is(0)))
        .andReturn().getResponse().getContentAsString();
    String sheetId = objectMapper.readTree(created).path("data").path("id").asText();

    mockMvc.perform(post("/api/inventory/outbound/sheet/confirm").param("id", sheetId)
            .header("Authorization", "Bearer " + token))
        .andExpect(jsonPath("$.code", not(is(0))))
        .andExpect(jsonPath("$.message", is("领用单需先提交物资领用审批，审批通过后才能出库")));

    OaApproval approval = new OaApproval();
    approval.setTenantId(1L);
    approval.setOrgId(1L);
    approval.setApprovalType("MATERIAL_APPLY");
    approval.setTitle("出库审批");
    approval.setApplicantId(500L);
    approval.setStatus("APPROVED");
    approval.setFormData("{\"outboundSheetId\":\"" + sheetId + "\"}");
    oaApprovalMapper.insert(approval);

    mockMvc.perform(post("/api/inventory/outbound/sheet/confirm").param("id", sheetId)
            .header("Authorization", "Bearer " + token))
        .andExpect(jsonPath("$.code", is(0)))
        .andExpect(jsonPath("$.data.status", is("CONFIRMED")));
  }

  private String login() throws Exception {
    String response = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"admin\",\"password\":\"123456\"}"))
        .andReturn().getResponse().getContentAsString();
    return objectMapper.readTree(response).path("data").path("token").asText();
  }
}
