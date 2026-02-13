package com.zhiyangyun.care.hr.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class StaffPointsRuleRequest {
  private Long id;
  private Long templateId;
  private Integer basePoints;
  private BigDecimal scoreWeight;
  private Integer suspiciousPenalty;
  private Integer failPoints;
  private Integer status;
  private String remark;
}
