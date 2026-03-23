package com.zhiyangyun.care.hr.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class HrSocialSecurityReminderResponse {
  private Long staffId;
  private String staffNo;
  private String staffName;
  private String phone;
  private Long departmentId;
  private String jobTitle;
  private LocalDate hireDate;
  private String socialSecurityStatus;
  private LocalDate socialSecurityStartDate;
  private Integer socialSecurityReminderDays;
  private Integer socialSecurityCompanyApply;
  private Integer socialSecurityNeedDirectorApproval;
  private String socialSecurityWorkflowStatus;
  private BigDecimal socialSecurityMonthlyAmount;
  private LocalDateTime socialSecurityApplySubmittedAt;
  private LocalDateTime socialSecurityDirectorDecisionAt;
  private LocalDateTime socialSecurityCompletedAt;
  private String socialSecurityLastBilledMonth;
  private LocalDate reminderDate;
  private Long remainingDays;
  private String reminderScope;
  private String socialSecurityRemark;
}
