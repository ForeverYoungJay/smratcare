package com.zhiyangyun.care.finance.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class PaymentRequest {
  /** 实收现金。全额由抵扣覆盖时可为 0。 */
  @NotNull
  @DecimalMin(value = "0", message = "实收金额不能为负数")
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
  /** 长护险统筹抵扣。 */
  @DecimalMin(value = "0", message = "长护险抵扣不能为负数")
  private BigDecimal ltciDeductAmount;
  /** 折扣减免。 */
  @DecimalMin(value = "0", message = "折扣减免不能为负数")
  private BigDecimal discountAmount;
  @Size(max = 200)
  private String discountReason;
  /** 本次使用的消费券。 */
  @Valid
  private List<PaymentVoucherUse> voucherUses;
}
