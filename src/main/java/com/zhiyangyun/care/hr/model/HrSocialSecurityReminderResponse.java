package com.zhiyangyun.care.hr.model;

import java.time.LocalDate;
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
  private LocalDate reminderDate;
  private Long remainingDays;
  private String reminderScope;
  private String socialSecurityRemark;
}
