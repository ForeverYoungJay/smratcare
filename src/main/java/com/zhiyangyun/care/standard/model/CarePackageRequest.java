package com.zhiyangyun.care.standard.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CarePackageRequest {
  private Long tenantId;
  private Long orgId;
  @NotBlank
  private String name;
  private String careLevel;
  private Integer cycleDays = 1;
  private Integer enabled = 1;
  private String remark;
  private Long createdBy;
}
