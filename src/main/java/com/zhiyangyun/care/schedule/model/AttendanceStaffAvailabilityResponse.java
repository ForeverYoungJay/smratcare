package com.zhiyangyun.care.schedule.model;

import lombok.Data;

@Data
public class AttendanceStaffAvailabilityResponse {
  private Long staffId;
  private String staffName;
  private Boolean available;
  private String status;
  private String message;
  private String contactPhone;
  private String staffNo;
}
