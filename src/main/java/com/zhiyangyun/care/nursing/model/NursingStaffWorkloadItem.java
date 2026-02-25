package com.zhiyangyun.care.nursing.model;

import lombok.Data;

@Data
public class NursingStaffWorkloadItem {
  private Long staffId;
  private String staffName;
  private Integer bookingCount;
  private Integer completedBookingCount;
  private Integer executeCount;
  private Integer completedExecuteCount;
  private Integer totalLoad;
}
