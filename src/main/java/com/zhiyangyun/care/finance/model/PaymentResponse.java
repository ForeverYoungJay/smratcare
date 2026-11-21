package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PaymentResponse {
  private Long billId;
  private BigDecimal paidAmount;
  private BigDecimal outstandingAmount;
  private String status;
  /** 本次实收现金。 */
  private BigDecimal cashAmount;
  private BigDecimal ltciDeductAmount;
  private BigDecimal discountAmount;
  private BigDecimal voucherAmount;
  /** 本次抵账合计 = 实收 + 三项抵扣。 */
  private BigDecimal settledAmount;
}
