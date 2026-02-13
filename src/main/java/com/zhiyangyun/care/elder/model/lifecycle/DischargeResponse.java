package com.zhiyangyun.care.elder.model.lifecycle;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class DischargeResponse {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long elderId;
  private LocalDate dischargeDate;
  private String reason;
  private BigDecimal settleAmount;
  private String remark;
}
