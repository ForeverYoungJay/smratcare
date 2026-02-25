package com.zhiyangyun.care.crm.model.report;

import lombok.Data;

@Data
public class MarketingConsultationTrendItem {
  private String date;
  private long total;
  private long consultCount;
  private long intentCount;
}
