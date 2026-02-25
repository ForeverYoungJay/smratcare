package com.zhiyangyun.care.crm.model.report;

import lombok.Data;

@Data
public class MarketingDataQualityResponse {
  private long missingSourceCount;
  private long missingNextFollowDateCount;
  private long nonStandardSourceCount;
}
