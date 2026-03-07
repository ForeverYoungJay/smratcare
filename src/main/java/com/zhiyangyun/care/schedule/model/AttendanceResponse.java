package com.zhiyangyun.care.schedule.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AttendanceResponse {
  private Long id;
  private Long staffId;
  private String staffName;
  private LocalDate workDate;
  private LocalDateTime checkInTime;
  private LocalDateTime checkOutTime;
  private LocalDateTime outingStartTime;
  private LocalDateTime outingEndTime;
  private LocalDateTime lunchBreakStartTime;
  private LocalDateTime lunchBreakEndTime;
  private Integer outingMinutes;
  private Integer reviewed;
  private Long reviewedBy;
  private LocalDateTime reviewedAt;
  private String reviewRemark;
  private String note;
  private String status;
}
