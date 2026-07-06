package com.zhiyangyun.care.ai.controller;

import com.zhiyangyun.care.ai.entity.AiGatewayConfig;
import com.zhiyangyun.care.ai.service.AiGatewayService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** AI 网关配置：当前 RULE_ENGINE，预留 LLM 扩展。 */
@RestController
@RequestMapping("/api/ai/gateway")
@PreAuthorize("hasAnyRole('DIRECTOR','SYS_ADMIN','ADMIN')")
public class AiGatewayConfigController {
  private final AiGatewayService gatewayService;

  public AiGatewayConfigController(AiGatewayService gatewayService) {
    this.gatewayService = gatewayService;
  }

  @GetMapping("/config")
  public Result<AiGatewayConfig> getConfig() {
    AiGatewayConfig config = gatewayService.getActiveConfig(AuthContext.getOrgId());
    // 不回传密钥明文
    if (config.getApiKey() != null && !config.getApiKey().isBlank()) {
      config.setApiKey("******");
    }
    return Result.ok(config);
  }

  @PostMapping("/config")
  public Result<AiGatewayConfig> saveConfig(@RequestBody AiGatewayConfig request) {
    AiGatewayConfig saved = gatewayService.saveConfig(AuthContext.getOrgId(), AuthContext.getStaffId(), request);
    if (saved.getApiKey() != null && !saved.getApiKey().isBlank()) {
      saved.setApiKey("******");
    }
    return Result.ok(saved);
  }
}
