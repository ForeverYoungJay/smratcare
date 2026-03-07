package com.zhiyangyun.care.finance.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class FinanceConfigImpactPreviewRequest {
  private String month;
  private List<String> configKeys = new ArrayList<>();
}
