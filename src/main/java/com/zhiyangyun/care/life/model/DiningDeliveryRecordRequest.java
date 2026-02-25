package com.zhiyangyun.care.life.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class DiningDeliveryRecordRequest {
  @NotNull
  private Long mealOrderId;

  @NotBlank
  private String orderNo;

  private Long deliveryAreaId;

  private String deliveryAreaName;

  private Long deliveredBy;

  private String deliveredByName;

  private LocalDateTime deliveredAt;

  private String status;

  private String remark;
}
