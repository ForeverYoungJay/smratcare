package com.zhiyangyun.care.smart.model;

import lombok.Data;

/** 标准事件上报处理结果。 */
@Data
public class SmartEventIngestResponse {
  private Long eventId;
  private Long deviceId;
  private boolean alertCreated;
  private Long alertId;
  private String alertLevel;
  private String matchedRuleCode;
  private boolean dispatchCreated;
  private Long dispatchId;
}
