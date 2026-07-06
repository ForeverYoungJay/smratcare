package com.zhiyangyun.care.ai.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/** 风险评分引擎输出：总分 + 等级 + 因子明细。 */
@Data
public class AiRiskEvaluation {
  private double score;
  /** LOW/MEDIUM/HIGH */
  private String level;
  private List<AiRiskFactorResult> factors = new ArrayList<>();
}
