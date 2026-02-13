package com.zhiyangyun.care.hr.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class StaffPointsLogResponse {
  private Long id;
  private Long staffId;
  private String staffName;
  private String changeType;
  private Integer changePoints;
  private Integer balanceAfter;
  private String sourceType;
  private Long sourceId;
  private String remark;
  private LocalDateTime createTime;
}
