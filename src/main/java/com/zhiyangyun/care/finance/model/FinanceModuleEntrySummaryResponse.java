package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class FinanceModuleEntrySummaryResponse {
  private String moduleKey;
  private LocalDate bizDate;
  private BigDecimal todayAmount = BigDecimal.ZERO;
  private BigDecimal monthAmount = BigDecimal.ZERO;
  private Long totalCount = 0L;
  private Long pendingCount = 0L;
  private Long exceptionCount = 0L;
  private String warningMessage;
  private List<EntryItem> topItems = new ArrayList<>();

  @Data
  public static class EntryItem {
    private String label;
    private Long count = 0L;
    private BigDecimal amount = BigDecimal.ZERO;
  }
}
