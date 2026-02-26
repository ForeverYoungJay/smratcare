package com.zhiyangyun.care.store.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ProductStockResponse {
  private Long id;
  private String idStr;
  private Long orgId;
  private String productCode;
  private String productName;
  private String category;
  private BigDecimal price;
  private Integer pointsPrice;
  private Integer safetyStock;
  private Integer currentStock;
  private Integer status;
  private String tagIds;
  private LocalDateTime createTime;
}
