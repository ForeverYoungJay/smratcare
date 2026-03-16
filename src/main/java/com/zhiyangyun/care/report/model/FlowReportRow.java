package com.zhiyangyun.care.report.model;

import java.time.LocalDate;
import lombok.Data;

@Data
public class FlowReportRow {
  private String eventType;
  private LocalDate eventDate;
  private Long elderId;
  private String elderName;
  private String remark;
}
