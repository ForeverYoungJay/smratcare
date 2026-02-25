package com.zhiyangyun.care.store.model.material;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class MaterialPurchaseOrderDetailResponse {
  private Long id;

  private String orderNo;

  private Long warehouseId;

  private String warehouseName;

  private Long supplierId;

  private String supplierName;

  private LocalDate orderDate;

  private String status;

  private BigDecimal totalAmount;

  private String remark;

  private LocalDateTime createTime;

  private List<MaterialPurchaseOrderItemResponse> items;
}
