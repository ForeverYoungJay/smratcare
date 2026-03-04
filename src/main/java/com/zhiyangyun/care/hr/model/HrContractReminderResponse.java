package com.zhiyangyun.care.hr.model;

import java.time.LocalDate;
import lombok.Data;

@Data
public class HrContractReminderResponse {
  private Long contractId;
  private String contractNo;
  private String elderName;
  private String contactName;
  private String contactPhone;
  private LocalDate effectiveFrom;
  private LocalDate effectiveTo;
  private Long remainingDays;
  private String status;
  private String contractStatus;
}
