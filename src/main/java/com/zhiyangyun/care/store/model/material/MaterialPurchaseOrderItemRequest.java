package com.zhiyangyun.care.store.model.material;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class MaterialPurchaseOrderItemRequest {
  @NotNull
  private Long productId;

  @NotNull
  private Integer quantity;

  @NotNull
  private BigDecimal unitPrice;
}
