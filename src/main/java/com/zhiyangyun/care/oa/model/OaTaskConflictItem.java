package com.zhiyangyun.care.oa.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OaTaskConflictItem {
  private Long taskId;
  private String title;
  private String assigneeName;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private String reason;
}
