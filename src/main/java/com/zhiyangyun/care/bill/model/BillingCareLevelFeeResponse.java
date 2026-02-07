package com.zhiyangyun.care.bill.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class BillingCareLevelFeeResponse {
  private Long id;
  private Long orgId;
  private String careLevel;
  private BigDecimal feeMonthly;
  private String effectiveMonth;
  private Integer status;
  private String remark;
}
