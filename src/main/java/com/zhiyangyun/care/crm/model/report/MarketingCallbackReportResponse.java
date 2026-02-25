package com.zhiyangyun.care.crm.model.report;

import java.util.List;
import lombok.Data;

@Data
public class MarketingCallbackReportResponse {
  private long todayDue;
  private long overdue;
  private long completed;
  private long total;
  private List<MarketingCallbackItem> records;
}
