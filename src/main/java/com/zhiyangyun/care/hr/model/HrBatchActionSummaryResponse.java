package com.zhiyangyun.care.hr.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class HrBatchActionSummaryResponse {
  private Integer totalCount;
  private Integer successCount;
  private Integer skippedCount;
  private BigDecimal totalAmount;
  private String message;
}
