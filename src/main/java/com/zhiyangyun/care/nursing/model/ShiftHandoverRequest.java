package com.zhiyangyun.care.nursing.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ShiftHandoverRequest {
  private Long tenantId;
  private Long orgId;
  @NotNull
  private LocalDate dutyDate;
  @NotBlank
  @Size(max = 64, message = "shiftCode too long")
  private String shiftCode;
  private Long fromStaffId;
  private Long toStaffId;
  @Size(max = 2000, message = "summary too long")
  private String summary;
  @Size(max = 2000, message = "riskNote too long")
  private String riskNote;
  @Size(max = 2000, message = "todoNote too long")
  private String todoNote;
  @Pattern(regexp = "^(DRAFT|HANDED_OVER|CONFIRMED)?$", message = "invalid status")
  private String status = "DRAFT";
  private LocalDateTime handoverTime;
  private LocalDateTime confirmTime;
  @Size(max = 2000, message = "attachmentUrls too long")
  private String attachmentUrls;
  private Long createdBy;
}
