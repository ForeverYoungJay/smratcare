package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PaymentResponse {
  private Long billId;
  private BigDecimal paidAmount;
  private BigDecimal outstandingAmount;
  private String status;
}
