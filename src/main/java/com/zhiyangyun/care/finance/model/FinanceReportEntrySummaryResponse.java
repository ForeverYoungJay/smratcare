package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class FinanceReportEntrySummaryResponse {
  private String reportKey;
  private String periodFrom;
  private String periodTo;
  private BigDecimal totalRevenue = BigDecimal.ZERO;
  private BigDecimal billedRevenue = BigDecimal.ZERO;
  private BigDecimal totalReceived = BigDecimal.ZERO;
  private BigDecimal refundTotal = BigDecimal.ZERO;
  private BigDecimal netRevenue = BigDecimal.ZERO;
  private BigDecimal totalStoreSales = BigDecimal.ZERO;
  private BigDecimal arrearsTotal = BigDecimal.ZERO;
  private Long arrearsElderCount = 0L;
  private String warningMessage;
  private List<NameAmountItem> topCategories = new ArrayList<>();
  private List<NameAmountItem> topRooms = new ArrayList<>();

  @Data
  public static class NameAmountItem {
    private String label;
    private BigDecimal amount = BigDecimal.ZERO;
    private String extra;
  }
}
