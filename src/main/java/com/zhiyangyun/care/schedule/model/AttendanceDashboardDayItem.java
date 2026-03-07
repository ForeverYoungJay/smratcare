package com.zhiyangyun.care.schedule.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AttendanceDashboardDayItem {
  private LocalDate date;
  private String weekLabel;
  private LocalDateTime checkInTime;
  private LocalDateTime checkOutTime;
  private LocalDateTime outingStartTime;
  private LocalDateTime outingEndTime;
  private LocalDateTime lunchBreakStartTime;
  private LocalDateTime lunchBreakEndTime;
  private Integer outingMinutes;
  private Integer lunchBreakMinutes;
  private Integer workMinutes;
  private String status;
  private Boolean late;
  private Boolean earlyLeave;
  private Boolean hasLeave;
  private String leaveTitle;
  private String leaveType;
  private String scheduleTitles;
  private String anomalyText;
}
