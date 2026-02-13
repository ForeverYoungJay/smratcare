package com.zhiyangyun.care.standard.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CarePackageItemRequest {
  private Long tenantId;
  private Long orgId;
  @NotNull
  private Long packageId;
  @NotNull
  private Long itemId;
  private Integer frequencyPerDay = 1;
  private Integer enabled = 1;
  private Integer sortNo = 0;
  private String remark;
  private Long createdBy;
}
