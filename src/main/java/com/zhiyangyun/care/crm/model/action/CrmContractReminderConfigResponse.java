package com.zhiyangyun.care.crm.model.action;

import lombok.Data;

@Data
public class CrmContractReminderConfigResponse {
  private Boolean enabled;
  private Integer advanceDays;
  private String sendTime;
  private String templateContent;
}
