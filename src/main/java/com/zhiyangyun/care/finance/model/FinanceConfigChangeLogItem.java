package com.zhiyangyun.care.finance.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class FinanceConfigChangeLogItem {
  private Long id;
  private String actorName;
  private Long actorId;
  private String actionType;
  private String entityType;
  private Long entityId;
  private String detail;
  private LocalDateTime createTime;
}
