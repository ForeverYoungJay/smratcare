package com.zhiyangyun.care.crm.model.action;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CrmSmsTaskResponse {
  private Long id;
  private Long leadId;
  private String phone;
  private String templateName;
  private String content;
  private LocalDateTime planSendTime;
  private String status;
  private LocalDateTime sendTime;
  private String resultMessage;
  private LocalDateTime createTime;
}
