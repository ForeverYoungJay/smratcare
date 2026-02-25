package com.zhiyangyun.care.life.model;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class RoomCleaningTaskRequest {
  @NotNull
  private Long roomId;

  @NotNull
  private LocalDate planDate;

  private String cleanerName;

  private String status;

  private String remark;
}
