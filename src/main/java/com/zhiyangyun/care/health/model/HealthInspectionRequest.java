package com.zhiyangyun.care.health.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;

@Data
public class HealthInspectionRequest {
  private Long elderId;
  @Size(max = 64, message = "elderName too long")
  private String elderName;

  @NotNull
  private LocalDate inspectionDate;

  @NotBlank
  @Size(max = 200, message = "inspectionItem too long")
  private String inspectionItem;

  @Size(max = 500, message = "result too long")
  private String result;
  @Pattern(regexp = "^(NORMAL|ABNORMAL|FOLLOWING|CLOSED)?$", message = "invalid inspection status")
  private String status;
  @Size(max = 64, message = "inspectorName too long")
  private String inspectorName;
  @Size(max = 1000, message = "followUpAction too long")
  private String followUpAction;
  @Size(max = 2000, message = "attachmentUrls too long")
  private String attachmentUrls;
  @Size(max = 1000, message = "otherNote too long")
  private String otherNote;
  @Size(max = 1000, message = "remark too long")
  private String remark;
}
