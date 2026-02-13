package com.zhiyangyun.care.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CareTaskTodayItem {
  private Long taskDailyId;
  private Long elderId;
  private String elderName;
  private Long bedId;
  private String roomNo;
  private Long staffId;
  private String staffName;
  private String taskName;
  private LocalDateTime planTime;
  private String status;
  private String careLevel;
  private Boolean suspiciousFlag;
}
