package com.zhiyangyun.care.finance.model;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class DischargeFeeAuditCreateRequest {
  @NotNull
  private Long elderId;
  private Long dischargeApplyId;
  @NotNull
  private BigDecimal payableAmount;
  private String feeItem;
  private String dischargeFeeConfig;
  private String remark;
}
