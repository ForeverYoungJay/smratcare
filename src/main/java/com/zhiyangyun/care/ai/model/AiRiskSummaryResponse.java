package com.zhiyangyun.care.ai.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/** 风险预测看板汇总。 */
@Data
public class AiRiskSummaryResponse {
  private long elderCount;
  private long highCount;
  private long mediumCount;
  private long lowCount;
  private List<TypeStat> typeStats = new ArrayList<>();

  @Data
  public static class TypeStat {
    private String riskType;
    private long highCount;
    private long mediumCount;
    private long lowCount;
  }
}
