package com.zhiyangyun.care.schedule.model;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ScheduleRequest {
  private Long id;
  @NotNull
  private Long staffId;
  @NotNull
  private LocalDate dutyDate;
  private String shiftCode;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private Integer status;
}
