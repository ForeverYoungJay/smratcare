package com.zhiyangyun.care.compliance.controller;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.compliance.model.SecurityPolicyUpdateRequest;
import com.zhiyangyun.care.compliance.model.SecurityPolicyView;
import com.zhiyangyun.care.compliance.service.ComplianceSecurityPolicyService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 机构安全策略配置接口：2FA / 密码有效期 / 会话超时 / 登录失败锁定 / 脱敏开关 / 日志留存。仅管理层可配置。
 */
@RestController
@RequestMapping("/api/compliance/security-policy")
public class ComplianceSecurityPolicyController {

  private final ComplianceSecurityPolicyService securityPolicyService;

  public ComplianceSecurityPolicyController(ComplianceSecurityPolicyService securityPolicyService) {
    this.securityPolicyService = securityPolicyService;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('DIRECTOR','SYS_ADMIN','ADMIN')")
  public Result<SecurityPolicyView> get() {
    Long orgId = AuthContext.getOrgId();
    return Result.ok(securityPolicyService.getEffectivePolicy(orgId));
  }

  @PutMapping
  @PreAuthorize("hasAnyRole('DIRECTOR','SYS_ADMIN','ADMIN')")
  public Result<SecurityPolicyView> update(@RequestBody SecurityPolicyUpdateRequest request) {
    Long orgId = AuthContext.getOrgId();
    return Result.ok(securityPolicyService.update(orgId, request));
  }
}
