package com.zhiyangyun.care.nursing.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import lombok.Data;

@Data
public class ShiftTemplateRequest {
  private Long tenantId;
  private Long orgId;
  @NotBlank
  private String name;
  @NotBlank
  private String shiftCode;
  @NotNull
  private LocalTime startTime;
  @NotNull
  private LocalTime endTime;
  private Integer crossDay = 0;
  private Integer requiredStaffCount = 1;
  private Integer enabled = 1;
  private String remark;
  private Long createdBy;
}
