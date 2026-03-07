package com.zhiyangyun.care.schedule.model;

import java.util.List;
import lombok.Data;

@Data
public class AttendanceDashboardOverviewResponse {
  private Long staffId;
  private String staffName;
  private String month;
  private String seasonType;
  private String expectedWorkStart;
  private String expectedWorkEnd;
  private Integer lateGraceMinutes;
  private Integer earlyLeaveGraceMinutes;
  private Integer outingMaxMinutes;
  private Long onDutyDays;
  private Long leaveDays;
  private Long lateCount;
  private Long earlyLeaveCount;
  private Long abnormalCount;
  private Integer totalOutingMinutes;
  private Integer totalLunchBreakMinutes;
  private String todayStatus;
  private String todayStatusLabel;
  private List<AttendanceDashboardDayItem> days;
}
