package com.zhiyangyun.care.store.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryOutboundRequest {
  private Long orgId;
  @NotNull
  private Long productId;
  private Long warehouseId;
  private Long batchId;
  @NotNull
  private Integer quantity;
  private String reason;
  private Long operatorStaffId;
}
