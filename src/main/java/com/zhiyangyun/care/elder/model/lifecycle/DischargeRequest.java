package com.zhiyangyun.care.elder.model.lifecycle;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class DischargeRequest {
  private Long tenantId;
  private Long orgId;
  @NotNull
  private Long elderId;
  @NotNull
  private LocalDate dischargeDate;
  private String reason;
  private BigDecimal settleAmount;
  private String remark;
  private Long createdBy;
}
