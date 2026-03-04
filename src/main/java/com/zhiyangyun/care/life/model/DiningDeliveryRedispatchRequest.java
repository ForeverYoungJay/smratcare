package com.zhiyangyun.care.life.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class DiningDeliveryRedispatchRequest {
  private LocalDateTime redispatchAt;

  private String redispatchByName;

  private String redispatchRemark;
}
