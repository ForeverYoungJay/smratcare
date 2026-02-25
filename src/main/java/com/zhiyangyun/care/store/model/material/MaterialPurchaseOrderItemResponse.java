package com.zhiyangyun.care.store.model.material;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class MaterialPurchaseOrderItemResponse {
  private Long id;

  private Long productId;

  private String productName;

  private Integer quantity;

  private BigDecimal unitPrice;

  private BigDecimal amount;
}
