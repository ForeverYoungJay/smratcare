package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class FinanceVoucherUsageView {
  private Long id;
  private Long voucherId;
  private String voucherNo;
  private Long elderId;
  private String elderName;
  private Long billMonthlyId;
  private String billMonth;
  private Long paymentRecordId;
  private BigDecimal amount;
  private String direction;
  private String directionText;
  private String remark;
  private LocalDateTime createTime;
}
