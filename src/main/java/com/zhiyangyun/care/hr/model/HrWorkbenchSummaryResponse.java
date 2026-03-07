package com.zhiyangyun.care.hr.model;

import lombok.Data;

@Data
public class HrWorkbenchSummaryResponse {
  private Long onJobCount;
  private Long leftCount;
  private Long todayTrainingCount;
  private Long pendingLeaveApprovalCount;
  private Long attendanceAbnormalCount;
  private Long contractExpiringCount;
  private Long birthdayTodayCount;
  private Long birthdayUpcomingCount;
  private Long birthdayTodoCount;
  private Integer warningDays;
}
