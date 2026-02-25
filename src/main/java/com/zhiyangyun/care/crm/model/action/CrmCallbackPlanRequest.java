package com.zhiyangyun.care.crm.model.action;

import lombok.Data;

@Data
public class CrmCallbackPlanRequest {
  private String title;
  private String planExecuteTime;
  private String executorName;
}
