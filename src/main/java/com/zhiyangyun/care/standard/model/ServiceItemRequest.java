package com.zhiyangyun.care.standard.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ServiceItemRequest {
  private Long tenantId;
  private Long orgId;
  @NotBlank
  private String name;
  private String category;
  private Integer defaultDuration;
  private Integer defaultPoints;
  private Integer enabled = 1;
  private String remark;
  private Long createdBy;
}
