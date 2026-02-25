package com.zhiyangyun.care.crm.model.action;

import java.util.List;
import lombok.Data;

@Data
public class CrmSmsTaskCreateRequest {
  private List<Long> leadIds;
  private String templateName;
  private String content;
  private String planSendTime;
}
