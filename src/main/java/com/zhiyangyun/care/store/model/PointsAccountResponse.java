package com.zhiyangyun.care.store.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PointsAccountResponse {
  private Long id;
  private Long orgId;
  private Long elderId;
  private String elderName;
  private Integer pointsBalance;
  private Integer status;
  private LocalDateTime updateTime;
}
