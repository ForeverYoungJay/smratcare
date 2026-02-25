package com.zhiyangyun.care.elder.model.lifecycle;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OutingCreateRequest {
  @NotNull
  private Long elderId;
  @NotNull
  private LocalDate outingDate;
  private LocalDateTime expectedReturnTime;
  private String companion;
  private String reason;
  private String remark;
}
