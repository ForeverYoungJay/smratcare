package com.zhiyangyun.care.crm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class CrmContractAssessmentContractItem {
  private Long leadId;
  private String contractNo;
  private String contractStatus;
  private String flowStage;
  private String currentOwnerDept;
  private String marketerName;
  private String orgName;
  private LocalDateTime contractSignedAt;
  private LocalDate contractExpiryDate;
  private List<CrmContractAssessmentReportItem> reports = new ArrayList<>();
}
