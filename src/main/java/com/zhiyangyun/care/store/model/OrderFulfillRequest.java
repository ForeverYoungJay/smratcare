package com.zhiyangyun.care.store.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderFulfillRequest {
  @NotNull
  private Long orderId;
}
