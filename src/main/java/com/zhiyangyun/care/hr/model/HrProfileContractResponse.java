package com.zhiyangyun.care.hr.model;

import java.time.LocalDate;
import lombok.Data;

@Data
public class HrProfileContractResponse {
  private Long contractId;
  private Long staffId;
  private String staffNo;
  private String staffName;
  private String phone;
  private String jobTitle;
  private String employmentType;
  private String contractNo;
  private String contractStatus;
  private String contractType;
  private LocalDate contractStartDate;
  private LocalDate contractEndDate;
  private Long remainingDays;
}
