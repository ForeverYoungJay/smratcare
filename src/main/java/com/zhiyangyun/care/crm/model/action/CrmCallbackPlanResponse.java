package com.zhiyangyun.care.crm.model.action;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CrmCallbackPlanResponse {
  @JsonSerialize(using = ToStringSerializer.class)
  private Long id;
  @JsonSerialize(using = ToStringSerializer.class)
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
