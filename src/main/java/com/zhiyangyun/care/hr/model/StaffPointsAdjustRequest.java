package com.zhiyangyun.care.hr.model;

import lombok.Data;

@Data
public class StaffPointsAdjustRequest {
  private Long staffId;
  private String changeType;
  private Integer changePoints;
  private String remark;
}
