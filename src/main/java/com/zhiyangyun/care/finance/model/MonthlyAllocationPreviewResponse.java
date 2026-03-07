package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class MonthlyAllocationPreviewResponse {
  private String allocationMonth;
  private String allocationName;
  private BigDecimal totalAmount;
  private Integer targetCount;
  private BigDecimal avgAmount;
  private String remark;
  private List<Row> rows = new ArrayList<>();

  @Data
  public static class Row {
    private Long elderId;
    private String elderName;
    private BigDecimal amount;
  }
}
