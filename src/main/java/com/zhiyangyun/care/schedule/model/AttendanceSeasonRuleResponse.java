package com.zhiyangyun.care.schedule.model;

import lombok.Data;

@Data
public class AttendanceSeasonRuleResponse {
  private String winterWorkStart;
  private String winterWorkEnd;
  private String summerWorkStart;
  private String summerWorkEnd;
  private Integer lateGraceMinutes;
  private Integer earlyLeaveGraceMinutes;
  private Integer outingMaxMinutes;
  private Integer lateEnabled;
  private Integer earlyLeaveEnabled;
  private Integer outingOvertimeEnabled;
  private Integer missingCheckoutEnabled;
  private String enabledStatus;
}
