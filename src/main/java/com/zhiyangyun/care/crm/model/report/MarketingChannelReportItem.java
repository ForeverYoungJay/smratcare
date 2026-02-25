package com.zhiyangyun.care.crm.model.report;

import lombok.Data;

@Data
public class MarketingChannelReportItem {
  private String source;
  private long leadCount;
  private long reservationCount;
  private long contractCount;
}
