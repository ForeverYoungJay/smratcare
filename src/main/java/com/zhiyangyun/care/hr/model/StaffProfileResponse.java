package com.zhiyangyun.care.hr.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class StaffProfileResponse {
  private Long staffId;
  private String staffNo;
  private String realName;
  private String phone;
  private Long departmentId;

  private String jobTitle;
  private String employmentType;
  private String contractNo;
  private String contractType;
  private String contractStatus;
  private LocalDate contractStartDate;
  private LocalDate contractEndDate;
  private LocalDate hireDate;
  private String qualificationLevel;
  private String certificateNo;
  private String emergencyContactName;
  private String emergencyContactPhone;
  private LocalDate birthday;
  private String socialSecurityStatus;
  private LocalDate socialSecurityStartDate;
  private Integer socialSecurityReminderDays;
  private String socialSecurityRemark;
  private Integer status;
  private LocalDate leaveDate;
  private String leaveReason;
  private String remark;
  private LocalDateTime updateTime;
}
