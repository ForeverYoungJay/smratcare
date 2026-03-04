package com.zhiyangyun.care.hr.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class HrCardSyncItemResponse {
  private Long cardAccountId;
  private Long elderId;
  private String elderName;
  private String cardNo;
  private String cardStatus;
  private Integer lossFlag;
  private LocalDateTime openTime;
  private LocalDateTime lastRechargeTime;
  private String syncStatus;
  private String remark;
}
