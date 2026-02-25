package com.zhiyangyun.care.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CareTaskExecuteLogItem {
  private Long id;
  private Long taskDailyId;
  private Long staffId;
  private String staffName;
  private LocalDateTime executeTime;
  private String bedQrCode;
  private Integer resultStatus;
  private Boolean suspiciousFlag;
  private String remark;
}
