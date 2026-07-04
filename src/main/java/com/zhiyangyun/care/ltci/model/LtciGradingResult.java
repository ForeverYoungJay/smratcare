package com.zhiyangyun.care.ltci.model;

import java.math.BigDecimal;
import lombok.Data;

/** 失能等级判级结果。 */
@Data
public class LtciGradingResult {
  private BigDecimal adlScore;
  private BigDecimal cognitiveScore;
  private BigDecimal perceptionScore;
  private BigDecimal totalScore;
  private int disabilityLevel;
  private String levelLabel;
  private boolean escalated;
}
