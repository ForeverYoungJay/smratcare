package com.zhiyangyun.care.bill.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class BillingConfigResponse {
  private Long id;
  private Long orgId;
  private String configKey;
  private BigDecimal configValue;
  private String effectiveMonth;
  private Integer status;
  private String remark;
}
