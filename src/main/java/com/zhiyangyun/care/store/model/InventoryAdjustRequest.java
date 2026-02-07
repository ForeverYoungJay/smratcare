package com.zhiyangyun.care.store.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryAdjustRequest {
  private Long orgId;
  @NotNull
  private Long productId;
  private Long batchId;
  @NotBlank
  private String adjustType; // GAIN/LOSS
  @NotNull
  private Integer adjustQty;
  private String reason;
  private Long operatorStaffId;
}
