package com.zhiyangyun.care.hr.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class StaffProfileRequest {
  private Long staffId;
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
  private Integer socialSecurityCompanyApply;
  private Integer socialSecurityNeedDirectorApproval;
  private String socialSecurityWorkflowStatus;
  private BigDecimal socialSecurityMonthlyAmount;
  private String socialSecurityLastBilledMonth;
  private String socialSecurityRemark;
  private Integer status;
  private LocalDate leaveDate;
  private String leaveReason;
  private String remark;
}
