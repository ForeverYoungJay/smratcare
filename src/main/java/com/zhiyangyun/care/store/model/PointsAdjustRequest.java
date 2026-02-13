package com.zhiyangyun.care.store.model;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class PointsAdjustRequest {
  private Long elderId;

  @NotNull
  private Integer points;

  @NotNull
  private String direction; // CREDIT / DEBIT

  private String remark;

  private Long orgId;
  private Long operatorId;
}
