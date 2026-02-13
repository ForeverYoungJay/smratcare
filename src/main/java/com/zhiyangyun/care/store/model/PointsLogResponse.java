package com.zhiyangyun.care.store.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PointsLogResponse {
  private Long id;
  private Long elderId;
  private String elderName;
  private String changeType;
  private Integer changePoints;
  private Integer balanceAfter;
  private Long refOrderId;
  private String remark;
  private LocalDateTime createTime;
}
