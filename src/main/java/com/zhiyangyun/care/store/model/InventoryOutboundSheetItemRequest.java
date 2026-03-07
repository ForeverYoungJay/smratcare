package com.zhiyangyun.care.store.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryOutboundSheetItemRequest {
  @NotNull
  private Long productId;

  private Long warehouseId;

  private Long batchId;

  @NotNull
  @Min(1)
  private Integer quantity;

  private String reason;
}
