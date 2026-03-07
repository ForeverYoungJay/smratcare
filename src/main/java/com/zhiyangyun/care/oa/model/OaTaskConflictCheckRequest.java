package com.zhiyangyun.care.oa.model;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class OaTaskConflictCheckRequest {
  private Long taskId;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private String assigneeName;
  private List<Long> collaboratorIds;
}
