package com.zhiyangyun.care.standard.model;

import java.time.LocalDate;
import lombok.Data;

@Data
public class ElderPackageResponse {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long elderId;
  private String elderName;
  private Long packageId;
  private String packageName;
  private LocalDate startDate;
  private LocalDate endDate;
  private Integer status;
  private String remark;
}
