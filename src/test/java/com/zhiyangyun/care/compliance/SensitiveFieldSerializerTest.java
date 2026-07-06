package com.zhiyangyun.care.compliance;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.compliance.annotation.SensitiveField;
import com.zhiyangyun.care.compliance.annotation.SensitiveType;
import com.zhiyangyun.care.compliance.config.SensitiveMaskingSupport;
import com.zhiyangyun.care.compliance.model.SecurityPolicyUpdateRequest;
import com.zhiyangyun.care.compliance.model.SecurityPolicyView;
import com.zhiyangyun.care.compliance.service.ComplianceSecurityPolicyService;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class SensitiveFieldSerializerTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Data
  static class DemoDto {
    @SensitiveField(type = SensitiveType.NAME)
    private String name;
    @SensitiveField(type = SensitiveType.PHONE)
    private String phone;
    @SensitiveField(type = SensitiveType.ID_CARD)
    private String idCardNo;
    @SensitiveField(type = SensitiveType.MEDICAL_SUMMARY)
    private String medicalSummary;
    private String remark;
  }

  @AfterEach
  void cleanup() {
    SecurityContextHolder.clearContext();
    SensitiveMaskingSupport.setPolicyService(null);
  }

  @Test
  void should_mask_when_org_masking_enabled_and_role_not_exempt() throws Exception {
    login("NURSING_EMPLOYEE");
    SensitiveMaskingSupport.setPolicyService(stubPolicy(true, List.of("SYS_ADMIN", "ADMIN", "DIRECTOR")));

    String json = objectMapper.writeValueAsString(demo());
    assertTrue(json.contains("\"name\":\"张*\""), json);
    assertTrue(json.contains("\"phone\":\"138****1234\""), json);
    assertTrue(json.contains("\"idCardNo\":\"3301********123X\""), json);
    assertTrue(json.contains("\"medicalSummary\":\"高血****\""), json);
    // 未标注字段不受影响
    assertTrue(json.contains("\"remark\":\"普通备注\""), json);
  }

  @Test
  void should_not_mask_for_exempt_role() throws Exception {
    login("SYS_ADMIN");
    SensitiveMaskingSupport.setPolicyService(stubPolicy(true, List.of("SYS_ADMIN", "ADMIN", "DIRECTOR")));

    String json = objectMapper.writeValueAsString(demo());
    assertTrue(json.contains("\"phone\":\"13800001234\""), json);
    assertTrue(json.contains("\"idCardNo\":\"33010219900101123X\""), json);
  }

  @Test
  void should_not_mask_when_org_switch_disabled() throws Exception {
    login("NURSING_EMPLOYEE");
    SensitiveMaskingSupport.setPolicyService(stubPolicy(false, List.of()));

    String json = objectMapper.writeValueAsString(demo());
    assertTrue(json.contains("\"phone\":\"13800001234\""), json);
  }

  @Test
  void should_not_mask_when_policy_service_absent() throws Exception {
    login("NURSING_EMPLOYEE");
    SensitiveMaskingSupport.setPolicyService(null);

    String json = objectMapper.writeValueAsString(demo());
    assertTrue(json.contains("\"phone\":\"13800001234\""), json);
  }

  private static DemoDto demo() {
    DemoDto dto = new DemoDto();
    dto.setName("张三");
    dto.setPhone("13800001234");
    dto.setIdCardNo("33010219900101123X");
    dto.setMedicalSummary("高血压二级，伴糖尿病");
    dto.setRemark("普通备注");
    return dto;
  }

  private static void login(String roleCode) {
    var auth = new UsernamePasswordAuthenticationToken(
        "9001", "N/A", List.of(new SimpleGrantedAuthority("ROLE_" + roleCode)));
    auth.setDetails(Map.of(
        "orgId", 1L,
        "username", "tester",
        "roleCodes", List.of(roleCode)));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  private static ComplianceSecurityPolicyService stubPolicy(boolean maskingEnabled, List<String> exemptRoles) {
    return new ComplianceSecurityPolicyService() {
      @Override
      public SecurityPolicyView getEffectivePolicy(Long orgId) {
        SecurityPolicyView view = new SecurityPolicyView();
        view.setOrgId(orgId);
        view.setMaskingEnabled(maskingEnabled);
        view.setMaskingExemptRoles(exemptRoles);
        return view;
      }

      @Override
      public SecurityPolicyView update(Long orgId, SecurityPolicyUpdateRequest request) {
        throw new UnsupportedOperationException();
      }
    };
  }
}
