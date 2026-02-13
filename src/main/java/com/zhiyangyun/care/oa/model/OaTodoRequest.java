package com.zhiyangyun.care.oa.model;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OaTodoRequest {
  @NotBlank
  private String title;

  private String content;

  private LocalDateTime dueTime;

  private String status;

  private Long assigneeId;

  private String assigneeName;
}
