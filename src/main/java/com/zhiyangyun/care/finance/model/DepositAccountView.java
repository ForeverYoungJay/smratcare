package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class DepositAccountView {
  private Long id;
  private Long elderId;
  private String elderName;
  private String careLevel;
  private String roomNo;
  private String bedNo;
  private BigDecimal standardAmount;
  private BigDecimal paidAmount;
  private BigDecimal deductedAmount;
  private BigDecimal refundedAmount;
  private BigDecimal balanceAmount;
  /** 应缴差额 = 标准 − 已缴，>0 表示未缴清。 */
  private BigDecimal shortfallAmount;
  private String status;
  private String statusText;
  private LocalDateTime lastOpAt;
  private String remark;
}
