package com.zhiyangyun.care.finance.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PaymentRequest {
  @NotNull
  private BigDecimal amount;
  @NotBlank
  private String method;
  @NotNull
  private LocalDateTime paidAt;
  @Size(max = 200)
  private String remark;
  @Size(max = 64)
  private String externalTxnId;
  private Long crossPeriodApprovalId;
}
