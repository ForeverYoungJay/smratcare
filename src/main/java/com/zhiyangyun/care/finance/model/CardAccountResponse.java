package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CardAccountResponse {
  private Long id;
  private Long elderId;
  private String elderName;
  private String cardNo;
  private String status;
  private Integer lossFlag;
  private BigDecimal balance;
  private BigDecimal creditLimit;
  private LocalDateTime openTime;
  private LocalDateTime lastRechargeTime;
  private String remark;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
