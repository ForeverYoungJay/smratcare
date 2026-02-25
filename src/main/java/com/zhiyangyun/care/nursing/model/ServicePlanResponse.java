package com.zhiyangyun.care.nursing.model;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Data;

@Data
public class ServicePlanResponse {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long elderId;
  private String elderName;
  private Long careLevelId;
  private String careLevelName;
  private Long serviceItemId;
  private String serviceItemName;
  private String planName;
  private String cycleType;
  private Integer frequency;
  private LocalDate startDate;
  private LocalDate endDate;
  private LocalTime preferredTime;
  private Long defaultStaffId;
  private String defaultStaffName;
  private String status;
  private String remark;
}
