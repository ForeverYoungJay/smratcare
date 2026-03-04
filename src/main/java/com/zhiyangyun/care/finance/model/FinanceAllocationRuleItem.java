package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class FinanceAllocationRuleItem {
  private Long id;
  private String ruleType;
  private String ruleTypeLabel;
  private String configKey;
  private BigDecimal configValue;
  private String effectiveMonth;
  private Integer status;
  private String remark;
}
