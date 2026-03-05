package com.zhiyangyun.care.life.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class DiningDeliveryRecordRequest {
  private Long mealOrderId;

  private String orderNo;

  private Long deliveryAreaId;

  private String deliveryAreaName;

  private Long deliveredBy;

  private String deliveredByName;

  private LocalDateTime deliveredAt;

  private String status;

  private String failureReason;

  private String redispatchStatus;

  private LocalDateTime redispatchAt;

  private String redispatchByName;

  private String redispatchRemark;

  private String remark;
}
