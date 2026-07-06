package com.zhiyangyun.care.ai.model;

import java.math.BigDecimal;
import lombok.Data;

/** 智能排班：约束配置保存请求。 */
@Data
public class AiScheduleConstraintRequest {
  private BigDecimal maxWeeklyHours;
  private Integer maxConsecutiveDays;
  private Integer nightRestEnabled;
  private Integer respectLeave;
  /** {"shiftCode":["ROLE_CODE",...]} */
  private String qualificationJson;
  private BigDecimal workloadBalanceWeight;
  private BigDecimal nightFairnessWeight;
  private String remark;
}
