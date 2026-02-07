package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ReconcileResponse {
  private LocalDate date;
  private BigDecimal totalReceived;
  private boolean mismatch;
}
