package com.zhiyangyun.care.store.model;

import lombok.Data;

@Data
public class InventoryOutboundSheetItemResponse {
  private Long id;
  private Long productId;
  private String productName;
  private String productCode;
  private String unit;
  private Long warehouseId;
  private Long batchId;
  private String batchNo;
  private Integer quantity;
  private String reason;
}
