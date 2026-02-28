package com.zhiyangyun.care.crm.model.action;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CrmCallbackPlanResponse {
  private Long id;
  private Long leadId;
  private String title;
  private String followupContent;
  private LocalDateTime planExecuteTime;
  private String executorName;
  private String status;
  private LocalDateTime executedTime;
  private String executeNote;
  private String followupResult;
  private LocalDateTime createTime;
}
