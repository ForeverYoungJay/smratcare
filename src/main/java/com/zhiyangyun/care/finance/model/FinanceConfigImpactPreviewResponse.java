package com.zhiyangyun.care.finance.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class FinanceConfigImpactPreviewResponse {
  private String month;
  private Long activeBillCount;
  private Long activeElderCount;
  private Long highRiskBillCount;
  private Long pendingApprovalCount;
  private Long recentPaymentCount;
  private Long allocationTaskCount;
  private Integer configKeyCount;
  private List<Item> impactedItems = new ArrayList<>();
  private List<String> riskTips = new ArrayList<>();

  @Data
  public static class Item {
    private String configKey;
    private String moduleLabel;
    private Long affectedCount;
    private String impactHint;
  }
}
