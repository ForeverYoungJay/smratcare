package com.zhiyangyun.care.elder.model.lifecycle;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class DeathRegisterCreateRequest {
  @NotNull
  private Long elderId;
  @NotNull
  private LocalDate deathDate;
  private LocalDateTime deathTime;
  private String place;
  private String cause;
  private String certificateNo;
  private String reportedBy;
  private LocalDateTime reportedTime;
  private String remark;
}
