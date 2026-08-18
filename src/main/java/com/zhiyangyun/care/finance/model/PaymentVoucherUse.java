package com.zhiyangyun.care.finance.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

/** 收款登记时使用的单张消费券。 */
@Data
public class PaymentVoucherUse {
  @NotNull
  private Long voucherId;
  @NotNull
  @DecimalMin(value = "0.01", message = "消费券抵扣金额必须大于 0")
  private BigDecimal amount;
}
