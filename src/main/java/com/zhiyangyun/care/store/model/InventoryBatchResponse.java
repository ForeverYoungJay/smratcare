package com.zhiyangyun.care.store.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class InventoryBatchResponse {
  private Long id;
  private Long productId;
  private String productName;
  private Integer safetyStock;
  private String batchNo;
  private Integer quantity;
  private BigDecimal costPrice;
  private LocalDate expireDate;
  private String warehouseLocation;
  private LocalDateTime createTime;
}
