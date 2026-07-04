package com.zhiyangyun.care.smart.model;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import lombok.Data;

/** 场景规则新增/编辑请求。 */
@Data
public class SmartAlertRuleRequest {
  @NotBlank
  private String ruleCode;
  @NotBlank
  private String ruleName;
  @NotBlank
  private String eventType;
  private String deviceType;
  private String metricKey;
  private String operator;
  private BigDecimal threshold;
  private BigDecimal threshold2;
  private Integer durationSec;
  private String level;
  private String disabilityLevelScope;
  private Integer autoDispatch;
  private Integer notifyFamily;
  private Integer priority;
  private Integer enabled;
  private String remark;
}
