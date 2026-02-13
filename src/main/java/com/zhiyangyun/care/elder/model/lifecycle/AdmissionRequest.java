package com.zhiyangyun.care.elder.model.lifecycle;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class AdmissionRequest {
  private Long tenantId;
  private Long orgId;
  @NotNull
  private Long elderId;
  @NotNull
  private LocalDate admissionDate;
  private String contractNo;
  private BigDecimal depositAmount;
  private Long bedId;
  private LocalDate bedStartDate;
  private String remark;
  private Long createdBy;
}
