package com.zhiyangyun.care.ai.service;

import com.zhiyangyun.care.ai.entity.AiGatewayConfig;
import com.zhiyangyun.care.ai.model.AiGatewayInvokeResult;
import java.util.Map;

/**
 * AI 网关适配层：统一封装"场景 + 载荷 → AI 结果"的调用入口。
 * 当前内置 RULE_ENGINE 实现（智能排班求解器 / 风险评分规则引擎均为本地纯逻辑），
 * 后续接入大模型时新增 LLM provider 实现即可，调用方无感知。
 */
public interface AiGatewayService {

  /** 取机构生效的网关配置（机构行优先，否则全局默认行）。 */
  AiGatewayConfig getActiveConfig(Long orgId);

  AiGatewayConfig saveConfig(Long orgId, Long staffId, AiGatewayConfig request);

  /**
   * @param scene 业务场景（如 SMART_SCHEDULE / RISK_SCORE）
   * @param payload 场景载荷
   */
  AiGatewayInvokeResult invoke(Long orgId, String scene, Map<String, Object> payload);
}
