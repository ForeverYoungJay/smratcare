package com.zhiyangyun.care.elder.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FamilyBindRequest {
  private Long orgId;
  private Long familyUserId;
  @NotNull
  private Long elderId;
  @NotBlank
  private String relation;
  private Integer isPrimary = 0;
  private String remark;
}
