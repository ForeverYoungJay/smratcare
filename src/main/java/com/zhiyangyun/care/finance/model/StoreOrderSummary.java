package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class StoreOrderSummary {
  private Long id;
  private String orderNo;
  private Long elderId;
  private String elderName;
  private BigDecimal totalAmount;
  private BigDecimal payableAmount;
  private Integer orderStatus;
  private Integer payStatus;
  private LocalDateTime payTime;
  private LocalDateTime createTime;
}
