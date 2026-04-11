package com.zhiyangyun.care.crm.model.trace;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CrmSalesReportSnapshotResponse {
  private Long id;
  private String snapshotType;
  private LocalDate snapshotDate;
  private LocalDate windowFrom;
  private LocalDate windowTo;
  private String snapshotKey;
  private String metricsJson;
  private Long generatedBy;
  private String generatedByName;
  private LocalDateTime generatedAt;
}
