package com.zhiyangyun.care.crm.model.report;

import lombok.Data;

@Data
public class MarketingCallbackItem {
  private Long id;
  private String name;
  private String phone;
  private String source;
  private String nextFollowDate;
  private String callbackType;
  private Double score;
  private String remark;
}
