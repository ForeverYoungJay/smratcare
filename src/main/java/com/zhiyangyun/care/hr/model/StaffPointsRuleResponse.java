package com.zhiyangyun.care.hr.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class StaffPointsRuleResponse {
  private Long id;
  private Long templateId;
  private String templateName;
  private Integer basePoints;
  private BigDecimal scoreWeight;
  private Integer suspiciousPenalty;
  private Integer failPoints;
  private Integer status;
  private String remark;
  private LocalDateTime updateTime;
}
