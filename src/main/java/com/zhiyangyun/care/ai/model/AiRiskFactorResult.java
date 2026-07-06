package com.zhiyangyun.care.ai.model;

import lombok.Data;

/** 单个风险因子的评估结果（写入 factor_json 明细）。 */
@Data
public class AiRiskFactorResult {
  private String code;
  private String name;
  /** 采集到的因子取值（缺失为 null，缺失因子不计分） */
  private Double value;
  private double score;

  public AiRiskFactorResult() {}

  public AiRiskFactorResult(String code, String name, Double value, double score) {
    this.code = code;
    this.name = name;
    this.value = value;
    this.score = score;
  }
}
