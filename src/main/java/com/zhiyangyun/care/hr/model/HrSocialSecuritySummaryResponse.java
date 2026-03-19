package com.zhiyangyun.care.hr.model;

import lombok.Data;

@Data
public class HrSocialSecuritySummaryResponse {
  private Long dueReminderCount;
  private Long upcomingReminderCount;
  private Long thisMonthNewParticipantCount;
}
