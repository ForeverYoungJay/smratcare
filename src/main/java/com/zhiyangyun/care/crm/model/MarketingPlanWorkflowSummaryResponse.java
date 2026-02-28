package com.zhiyangyun.care.crm.model;

import java.util.List;
import lombok.Data;

@Data
public class MarketingPlanWorkflowSummaryResponse {
  private Long planId;
  private long totalStaffCount;
  private long readCount;
  private long unreadCount;
  private long agreeCount;
  private long improveCount;
  private List<MarketingPlanReceiptResponse> receipts;
}
