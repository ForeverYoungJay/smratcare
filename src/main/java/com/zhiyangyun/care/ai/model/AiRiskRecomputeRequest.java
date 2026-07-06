package com.zhiyangyun.care.ai.model;

import lombok.Data;

/** 手动触发风险重算请求（elderId 为空则全员重算）。 */
@Data
public class AiRiskRecomputeRequest {
  private Long elderId;
  /** 为空则重算所有启用的风险类型 */
  private String riskType;
}
