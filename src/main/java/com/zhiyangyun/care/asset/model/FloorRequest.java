package com.zhiyangyun.care.asset.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FloorRequest {
  private Long tenantId;
  private Long orgId;
  @NotNull
  private Long buildingId;
  @NotBlank
  private String floorNo;
  private String name;
  private Integer status = 1;
  private Integer sortNo = 0;
  private Long createdBy;
}
