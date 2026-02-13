package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ElderAccountResponse {
  private Long id;
  private Long elderId;
  private String elderName;
  private BigDecimal balance;
  private BigDecimal creditLimit;
  private BigDecimal warnThreshold;
  private Integer status;
  private Integer pointsBalance;
  private Integer pointsStatus;
  private String remark;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
