package com.zhiyangyun.care.store.model;

import lombok.Data;

@Data
public class InventoryAdjustmentDiffItem {
  private Long productId;
  private String productName;
  private String category;
  private Long warehouseId;
  private String warehouseName;
  private Integer gainQty;
  private Integer lossQty;
  private Integer diffQty;
}
