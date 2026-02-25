package com.zhiyangyun.care.finance.model;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class AdmissionFeeAuditCreateRequest {
  @NotNull
  private Long elderId;
  private Long admissionId;
  @NotNull
  private BigDecimal totalAmount;
  @NotNull
  private BigDecimal depositAmount;
  private String remark;
}
