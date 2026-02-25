package com.zhiyangyun.care.store.model.material;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class MaterialStockAmountItem {
  private String dimension;

  private Long productId;

  private String productCode;

  private String productName;

  private Long warehouseId;

  private String warehouseName;

  private String category;

  private Integer totalQuantity;

  private BigDecimal totalAmount;
}
