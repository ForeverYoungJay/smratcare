package com.zhiyangyun.care.hr.model;

import java.time.LocalDate;
import lombok.Data;

@Data
public class StaffProfileRequest {
  private Long staffId;
  private String jobTitle;
  private String employmentType;
  private LocalDate hireDate;
  private String qualificationLevel;
  private String certificateNo;
  private String emergencyContactName;
  private String emergencyContactPhone;
  private Integer status;
  private LocalDate leaveDate;
  private String leaveReason;
  private String remark;
}
