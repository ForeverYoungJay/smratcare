package com.zhiyangyun.care.baseconfig.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BaseDataItemRequest {
  @NotBlank
  private String configGroup;
  @NotBlank
  private String itemCode;
  @NotBlank
  private String itemName;
  @NotNull
  private Integer status;
  private Integer sortNo;
  private String remark;
  private Long tenantId;
  private Long orgId;
  private Long createdBy;
}
