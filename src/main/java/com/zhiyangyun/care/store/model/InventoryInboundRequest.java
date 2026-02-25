package com.zhiyangyun.care.store.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class InventoryInboundRequest {
  private Long orgId;
  @NotNull
  private Long productId;
  private Long warehouseId;
  private String batchNo;
  @NotNull
  private Integer quantity;
  private BigDecimal costPrice;
  private LocalDate expireDate;
  private String warehouseLocation;
  private String remark;
}
