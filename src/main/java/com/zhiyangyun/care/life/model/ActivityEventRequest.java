package com.zhiyangyun.care.life.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ActivityEventRequest {
  @NotBlank
  private String title;

  @NotNull
  private LocalDate eventDate;

  private LocalDateTime startTime;

  private LocalDateTime endTime;

  private String location;

  private String organizer;

  private String content;

  private String status;

  private String remark;
}
