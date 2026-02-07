package com.zhiyangyun.care.store.model;

import java.time.LocalDate;
import lombok.Data;

@Data
public class InventoryExpiryAlertItem {
  private Long productId;
  private String productName;
  private Long batchId;
  private String batchNo;
  private Integer quantity;
  private LocalDate expireDate;
  private Integer daysToExpire;
}
