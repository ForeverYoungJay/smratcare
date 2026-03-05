package com.zhiyangyun.care.nursing.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
  private String shiftCode;
  @NotNull
  private Long fromStaffId;
  @NotNull
  private Long toStaffId;
  private String summary;
  private String riskNote;
  private String todoNote;
  private String status = "DRAFT";
  private LocalDateTime handoverTime;
  private LocalDateTime confirmTime;
  @Size(max = 2000, message = "attachmentUrls too long")
  private String attachmentUrls;
  private Long createdBy;
}
