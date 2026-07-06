package com.zhiyangyun.care.compliance.config;

import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.compliance.annotation.SensitiveType;
import com.zhiyangyun.care.compliance.model.SecurityPolicyView;
import com.zhiyangyun.care.compliance.service.ComplianceSecurityPolicyService;
import com.zhiyangyun.care.compliance.util.DataMaskingUtil;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * 敏感字段脱敏的运行时支撑：为 Jackson 序列化器（无法走 Spring 注入）桥接机构安全策略与当前登录上下文。
 *
 * <p>判定顺序：机构开关（compliance_security_policy.masking_enabled，默认关闭）→ 角色豁免
 * （masking_exempt_roles，默认 SYS_ADMIN/ADMIN/DIRECTOR 可看全文）→ 按类型打码。
 * 任何异常一律降级为原文输出，脱敏不得影响主业务流程。
 */
@Component
public class SensitiveMaskingSupport {

  private static volatile ComplianceSecurityPolicyService policyService;

  public SensitiveMaskingSupport(ComplianceSecurityPolicyService complianceSecurityPolicyService) {
    setPolicyService(complianceSecurityPolicyService);
  }

  /** 供单元测试注入桩实现；生产由 Spring 构造注入。 */
  public static void setPolicyService(ComplianceSecurityPolicyService service) {
    policyService = service;
  }

  /** 按当前登录上下文与机构策略决定是否脱敏；不脱敏时返回原文。 */
  public static String maskIfRequired(SensitiveType type, String value) {
    if (value == null || value.isBlank() || type == null) {
      return value;
    }
    try {
      if (!shouldMask()) {
        return value;
      }
      return mask(type, value);
    } catch (Exception ex) {
      // 脱敏判断/执行失败不得影响接口返回
      return value;
    }
  }

  /** 仅按类型打码，不做开关与豁免判断。 */
  public static String mask(SensitiveType type, String value) {
    if (value == null || type == null) {
      return value;
    }
    return switch (type) {
      case NAME -> DataMaskingUtil.maskName(value);
      case PHONE -> DataMaskingUtil.maskPhone(value);
      case ID_CARD -> DataMaskingUtil.maskIdCard(value);
      case ADDRESS -> DataMaskingUtil.maskAddress(value);
      case BANK_CARD -> DataMaskingUtil.maskBankCard(value);
      case MEDICAL_SUMMARY -> DataMaskingUtil.maskMedicalSummary(value);
    };
  }

  private static boolean shouldMask() {
    ComplianceSecurityPolicyService service = policyService;
    if (service == null) {
      // 未在 Spring 环境中初始化（如脚本/纯单元测试）时不脱敏
      return false;
    }
    Long orgId = AuthContext.getOrgId();
    if (orgId == null) {
      return false;
    }
    SecurityPolicyView policy = service.getEffectivePolicy(orgId);
    if (policy == null || !Boolean.TRUE.equals(policy.getMaskingEnabled())) {
      return false;
    }
    List<String> exemptRoles = policy.getMaskingExemptRoles();
    if (exemptRoles == null || exemptRoles.isEmpty()) {
      return true;
    }
    List<String> currentRoles = AuthContext.getRoleCodes();
    for (String role : currentRoles) {
      if (role == null) {
        continue;
      }
      if (exemptRoles.contains(role.toUpperCase(Locale.ROOT))) {
        return false;
      }
    }
    return true;
  }
}
