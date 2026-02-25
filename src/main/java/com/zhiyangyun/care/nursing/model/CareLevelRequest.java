package com.zhiyangyun.care.nursing.model;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CareLevelRequest {
  private Long tenantId;
  private Long orgId;
  @NotBlank
  private String levelCode;
  @NotBlank
  private String levelName;
  private Integer severity = 1;
  private BigDecimal monthlyFee;
  private Integer enabled = 1;
  private String remark;
  private Long createdBy;
}
