package com.zhiyangyun.care.elder.model.lifecycle;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class AdmissionResponse {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long elderId;
  private LocalDate admissionDate;
  private String contractNo;
  private BigDecimal depositAmount;
  private String remark;
}
