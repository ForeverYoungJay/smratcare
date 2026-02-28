package com.zhiyangyun.care.crm.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MarketingPlanReceiptResponse {
  private Long staffId;
  private String staffName;
  private LocalDateTime readTime;
  private String action;
  private String actionDetail;
  private LocalDateTime actionTime;
}
