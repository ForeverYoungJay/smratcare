package com.zhiyangyun.care.elder.model.lifecycle;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AdmissionRecordResponse {
  private Long id;
  private Long elderId;
  private String elderName;
  private String contractNo;
  private LocalDate admissionDate;
  private BigDecimal depositAmount;
  private String remark;
  private Integer elderStatus;
  private LocalDateTime createTime;
}
