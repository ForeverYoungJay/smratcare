package com.zhiyangyun.care.store.model.material;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MaterialWarehouseRequest {
  @NotBlank
  private String warehouseCode;

  @NotBlank
  private String warehouseName;

  private String managerName;

  private String managerPhone;

  private String address;

  private Integer status;

  private String remark;
}
