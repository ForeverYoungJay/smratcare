package com.zhiyangyun.care.bill.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class BillMonthlyView {
  private Long id;
  private Long orgId;
  private Long elderId;
  private String elderName;
  private String careLevel;
  private String billMonth;
  private BigDecimal totalAmount;
  private BigDecimal nursingFee;
  private BigDecimal bedFee;
  private BigDecimal insuranceFee;
  private BigDecimal paidAmount;
  private BigDecimal outstandingAmount;
  private String lastPayMethod;
  private Integer status;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
