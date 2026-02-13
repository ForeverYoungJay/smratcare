package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PaymentRecordItem {
  private Long id;
  private Long billMonthlyId;
  private BigDecimal amount;
  private String payMethod;
  private LocalDateTime paidAt;
  private Long operatorStaffId;
  private String operatorStaffName;
  private String remark;
  private String externalTxnId;
  private LocalDateTime createTime;
}
