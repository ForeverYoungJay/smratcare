package com.zhiyangyun.care.crm.model.report;

import lombok.Data;

@Data
public class MarketingConversionReportResponse {
  private long totalLeads;
  private long consultCount;
  private long intentCount;
  private long reservationCount;
  private long invalidCount;
  private long contractCount;
  private double intentRate;
  private double reservationRate;
  private double contractRate;
}
