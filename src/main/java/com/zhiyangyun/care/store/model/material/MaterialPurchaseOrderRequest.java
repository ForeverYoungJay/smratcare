package com.zhiyangyun.care.store.model.material;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class MaterialPurchaseOrderRequest {
  private Long warehouseId;

  private Long supplierId;

  private LocalDate orderDate;

  private String remark;

  @Valid
  @NotEmpty
  private List<MaterialPurchaseOrderItemRequest> items;
}
