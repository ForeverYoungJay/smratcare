package com.zhiyangyun.care.hr.model;

import java.time.LocalDate;
import lombok.Data;

@Data
public class HrProfileContractResponse {
  private Long contractId;
  private String contractNo;
  private String elderName;
  private String contactName;
  private String contactPhone;
  private String status;
  private String contractStatus;
  private String flowStage;
  private LocalDate effectiveFrom;
  private LocalDate effectiveTo;
  private Long remainingDays;
}
