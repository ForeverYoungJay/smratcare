package com.zhiyangyun.care.health.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import lombok.Data;

@Data
public class HealthInspectionRequest {
  private Long elderId;
  private String elderName;

  @NotNull
  private LocalDate inspectionDate;

  @NotBlank
  private String inspectionItem;

  private String result;
  @Pattern(regexp = "^(NORMAL|ABNORMAL|FOLLOWING|CLOSED)?$", message = "invalid inspection status")
  private String status;
  private String inspectorName;
  private String followUpAction;
  private String remark;
}
