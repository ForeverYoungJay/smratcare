package com.zhiyangyun.care.hr.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class StaffTrainingRequest {
  private Long staffId;
  private String trainingName;
  private String trainingType;
  private String provider;
  private LocalDate startDate;
  private LocalDate endDate;
  private BigDecimal hours;
  private BigDecimal score;
  private Integer status;
  private String certificateNo;
  private String remark;
}
