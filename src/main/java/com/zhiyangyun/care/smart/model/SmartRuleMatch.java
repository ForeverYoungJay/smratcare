package com.zhiyangyun.care.smart.model;

import lombok.Data;

/** 规则引擎匹配结果。 */
@Data
public class SmartRuleMatch {
  private boolean matched;
  private Long ruleId;
  private String ruleCode;
  private String ruleName;
  private String level;
  private boolean autoDispatch;
  private boolean notifyFamily;
  private String title;

  public static SmartRuleMatch noMatch() {
    SmartRuleMatch m = new SmartRuleMatch();
    m.matched = false;
    return m;
  }
}
