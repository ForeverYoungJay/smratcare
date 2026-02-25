package com.zhiyangyun.care.crm.model.action;

import lombok.Data;

@Data
public class CrmCallbackPlanExecuteRequest {
  private String executeNote;
  private String nextFollowDate;
}
