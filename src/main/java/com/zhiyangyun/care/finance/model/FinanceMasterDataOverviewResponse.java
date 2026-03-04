package com.zhiyangyun.care.finance.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class FinanceMasterDataOverviewResponse {
  private String month;
  private Long feeSubjectCount;
  private Long billingRuleCount;
  private Long paymentChannelCount;
  private Long pendingApprovalCount;
  private List<String> paymentChannels = new ArrayList<>();
  private List<ConfigEntry> recentConfigs = new ArrayList<>();

  @Data
  public static class ConfigEntry {
    private String configKey;
    private String effectiveMonth;
    private String valueText;
    private String remark;
    private Integer status;
  }
}
