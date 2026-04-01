package com.zhiyangyun.care.oa.model;

import java.util.List;
import lombok.Data;

@Data
public class OaMyInfoSummaryResponse {
  private String currentMonth;
  private String previousMonth;
  private String nextMonth;
  private boolean compareEnabled;
  private List<OaMyInfoItemResponse> items;
}
