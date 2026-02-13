package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ElderAccountUpdateRequest {
  private Long elderId;

  private String elderName;

  private BigDecimal creditLimit;

  private BigDecimal warnThreshold;

  private Integer status;

  private String remark;
}
