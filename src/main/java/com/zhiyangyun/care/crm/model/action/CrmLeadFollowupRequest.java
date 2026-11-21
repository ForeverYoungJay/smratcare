package com.zhiyangyun.care.crm.model.action;

import lombok.Data;

/** 记录一次已完成的跟进，可同时约定下次回访时间。 */
@Data
public class CrmLeadFollowupRequest {
  /** 跟进方式：电话回访 / 微信沟通 / 到院面谈 等，作为记录标题 */
  private String method;
  private String followupResult;
  private String followupContent;
  /** 下次回访时间（yyyy-MM-dd HH:mm:ss 或 ISO），为空则不创建下次提醒 */
  private String nextFollowTime;
  private String callbackType;
}
