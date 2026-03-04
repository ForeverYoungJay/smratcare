package com.zhiyangyun.care.hr.model;

import lombok.Data;

@Data
public class StaffPointsAdjustRequest {
  private Long staffId;
  private String changeType;
  private Integer changePoints;
  private String sourceType;
  private Long sourceId;
  private String remark;
}
