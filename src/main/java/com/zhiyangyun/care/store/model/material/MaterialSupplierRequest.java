package com.zhiyangyun.care.store.model.material;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MaterialSupplierRequest {
  @NotBlank
  private String supplierCode;

  @NotBlank
  private String supplierName;

  private String contactName;

  private String contactPhone;

  private String address;

  private Integer status;

  private String remark;
}
