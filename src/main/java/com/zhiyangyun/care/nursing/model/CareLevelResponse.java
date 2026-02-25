package com.zhiyangyun.care.nursing.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class CareLevelResponse {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private String levelCode;
  private String levelName;
  private Integer severity;
  private BigDecimal monthlyFee;
  private Integer enabled;
  private String remark;
}
