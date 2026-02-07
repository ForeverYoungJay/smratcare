package com.zhiyangyun.care.finance.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
  private String remark;
  private String externalTxnId;
}
