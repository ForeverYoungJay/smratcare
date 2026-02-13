package com.zhiyangyun.care.store.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRefundRequest {
  @NotNull
  private Long orderId;

  private String reason;
}
