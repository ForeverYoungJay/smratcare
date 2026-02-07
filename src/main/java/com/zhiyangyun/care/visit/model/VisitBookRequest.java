package com.zhiyangyun.care.visit.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class VisitBookRequest {
  private Long orgId;
  @NotNull
  private Long elderId;
  private Long familyUserId;
  @NotNull
  private LocalDateTime visitTime;
  @NotBlank
  private String visitTimeSlot;
  private Integer visitorCount = 1;
  private String carPlate;
  private String remark;
}
