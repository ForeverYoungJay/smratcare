package com.zhiyangyun.care.finance.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 押金缴纳/扣款/退还登记。 */
@Data
public class DepositTransactionRequest {
  @NotNull
  private Long elderId;
  @NotNull
  @DecimalMin(value = "0.01", message = "金额必须大于 0")
  private BigDecimal amount;
  @Size(max = 32)
  private String payMethod;
  private LocalDateTime occurredAt;
  /** 扣款与退还必填。 */
  @Size(max = 200)
  private String reason;
  @Size(max = 200)
  private String remark;
}
