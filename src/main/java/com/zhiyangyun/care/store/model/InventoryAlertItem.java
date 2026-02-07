package com.zhiyangyun.care.store.model;

import lombok.Data;

@Data
public class InventoryAlertItem {
  private Long productId;
  private String productName;
  private Integer safetyStock;
  private Integer currentStock;
}
