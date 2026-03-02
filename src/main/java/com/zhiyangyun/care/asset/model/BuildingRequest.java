package com.zhiyangyun.care.asset.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BuildingRequest {
  private Long tenantId;
  private Long orgId;
  @NotBlank
  private String name;
  private String code;
  private String areaCode;
  private String areaName;
  private Integer status = 1;
  private Integer sortNo = 0;
  private String remark;
  private Long createdBy;
}
