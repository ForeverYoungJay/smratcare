package com.zhiyangyun.care.crm.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class CrmContractAssessmentOverviewResponse {
  private Long elderId;
  private String elderName;
  private String elderPhone;
  private Integer totalContractCount;
  private Integer totalReportCount;
  private List<CrmContractAssessmentContractItem> contracts = new ArrayList<>();
  private List<CrmContractAssessmentReportItem> unassignedReports = new ArrayList<>();
}
