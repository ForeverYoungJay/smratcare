package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class FinanceCategoryConsumptionAnalysisResponse {
  private String itemKeyword;
  private String from;
  private String to;
  private BigDecimal totalAmount = BigDecimal.ZERO;
  private BigDecimal itemAmount = BigDecimal.ZERO;
  private BigDecimal itemRatio = BigDecimal.ZERO;
  private List<TrendItem> trend = new ArrayList<>();
  private List<CategoryProfitItem> categoryProfit = new ArrayList<>();

  @Data
  public static class TrendItem {
    private String period;
    private BigDecimal amount = BigDecimal.ZERO;
    private BigDecimal ratio = BigDecimal.ZERO;
  }

  @Data
  public static class CategoryProfitItem {
    private String category;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private BigDecimal totalCost = BigDecimal.ZERO;
    private BigDecimal totalProfit = BigDecimal.ZERO;
    private BigDecimal profitRate = BigDecimal.ZERO;
  }
}
