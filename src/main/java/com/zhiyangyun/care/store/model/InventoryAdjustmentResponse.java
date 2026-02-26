package com.zhiyangyun.care.store.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class InventoryAdjustmentResponse {
  private Long id;
  private Long productId;
  private String productName;
  private String category;
  private Long batchId;
  private Long warehouseId;
  private String warehouseName;
  private String inventoryType;
  private String adjustType;
  private Integer adjustQty;
  private String reason;
  private Long operatorStaffId;
  private LocalDateTime createTime;
}
