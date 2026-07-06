package com.zhiyangyun.care.ai.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.ai.entity.AiGatewayConfig;
import com.zhiyangyun.care.ai.mapper.AiGatewayConfigMapper;
import com.zhiyangyun.care.ai.model.AiGatewayInvokeResult;
import com.zhiyangyun.care.ai.service.AiGatewayService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * AI 网关默认实现：RULE_ENGINE 走本地规则引擎（当前排班/风险评分均为本地纯逻辑，
 * invoke 返回引擎标识供调用方直连对应 service）；LLM provider 为预留扩展点。
 */
@Service
public class AiGatewayServiceImpl implements AiGatewayService {
  public static final String PROVIDER_RULE_ENGINE = "RULE_ENGINE";
  public static final String PROVIDER_LLM = "LLM";

  private final AiGatewayConfigMapper configMapper;

  public AiGatewayServiceImpl(AiGatewayConfigMapper configMapper) {
    this.configMapper = configMapper;
  }

  @Override
  public AiGatewayConfig getActiveConfig(Long orgId) {
    AiGatewayConfig config = selectByOrg(orgId);
    if (config == null) {
      config = selectByOrg(null);
    }
    if (config == null) {
      config = new AiGatewayConfig();
      config.setProvider(PROVIDER_RULE_ENGINE);
      config.setModelName("builtin-rule-v1");
      config.setTimeoutMs(30000);
      config.setEnabled(1);
    }
    return config;
  }

  @Override
  public AiGatewayConfig saveConfig(Long orgId, Long staffId, AiGatewayConfig request) {
    AiGatewayConfig existing = selectByOrg(orgId);
    if (existing == null) {
      existing = new AiGatewayConfig();
      existing.setTenantId(orgId);
      existing.setOrgId(orgId);
      existing.setCreatedBy(staffId);
    }
    if (request.getProvider() != null && !request.getProvider().isBlank()) {
      existing.setProvider(request.getProvider().toUpperCase(java.util.Locale.ROOT));
    }
    existing.setEndpoint(request.getEndpoint());
    existing.setApiKey(request.getApiKey());
    existing.setModelName(request.getModelName());
    existing.setTimeoutMs(request.getTimeoutMs() == null ? 30000 : request.getTimeoutMs());
    existing.setEnabled(request.getEnabled() == null ? 1 : request.getEnabled());
    existing.setRemark(request.getRemark());
    if (existing.getId() == null) {
      configMapper.insert(existing);
    } else {
      configMapper.updateById(existing);
    }
    return existing;
  }

  @Override
  public AiGatewayInvokeResult invoke(Long orgId, String scene, Map<String, Object> payload) {
    AiGatewayConfig config = getActiveConfig(orgId);
    if (config.getEnabled() != null && config.getEnabled() == 0) {
      return AiGatewayInvokeResult.fail(config.getProvider(), scene, "AI 网关已停用");
    }
    if (PROVIDER_LLM.equalsIgnoreCase(config.getProvider())) {
      // 扩展点：接入大模型时在此按 endpoint/apiKey/modelName 发起调用
      return AiGatewayInvokeResult.fail(PROVIDER_LLM, scene, "LLM provider 尚未接入，当前请使用 RULE_ENGINE");
    }
    Map<String, Object> data = new HashMap<>();
    data.put("engine", config.getModelName() == null ? "builtin-rule-v1" : config.getModelName());
    data.put("mode", "LOCAL_RULE");
    if (payload != null) {
      data.put("payloadKeys", payload.keySet());
    }
    return AiGatewayInvokeResult.ok(PROVIDER_RULE_ENGINE, scene, data);
  }

  private AiGatewayConfig selectByOrg(Long orgId) {
    var wrapper = Wrappers.lambdaQuery(AiGatewayConfig.class)
        .eq(AiGatewayConfig::getIsDeleted, 0);
    if (orgId == null) {
      wrapper.isNull(AiGatewayConfig::getOrgId);
    } else {
      wrapper.eq(AiGatewayConfig::getOrgId, orgId);
    }
    return configMapper.selectOne(wrapper
        .orderByDesc(AiGatewayConfig::getId)
        .last("LIMIT 1"));
  }
}
