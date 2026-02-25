package com.zhiyangyun.care.finance.model;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ConsumptionRecordCreateRequest {
  @NotNull
  private Long elderId;
  @NotNull
  private LocalDate consumeDate;
  @NotNull
  private BigDecimal amount;
  private String category;
  private String sourceType;
  private Long sourceId;
  private String remark;
}
