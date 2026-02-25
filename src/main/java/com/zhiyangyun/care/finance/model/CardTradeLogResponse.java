package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CardTradeLogResponse {
  private Long id;
  private Long cardAccountId;
  private String cardNo;
  private Long elderId;
  private String elderName;
  private BigDecimal amount;
  private BigDecimal balanceAfter;
  private String direction;
  private String sourceType;
  private String remark;
  private LocalDateTime createTime;
}
