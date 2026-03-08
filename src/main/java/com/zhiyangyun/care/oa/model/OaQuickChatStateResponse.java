package com.zhiyangyun.care.oa.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OaQuickChatStateResponse {
  private Long id;
  private Long orgId;
  private Long staffId;
  private String stateJson;
  private LocalDateTime updateTime;
}
