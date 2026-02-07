package com.zhiyangyun.care.store.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderPreviewRequest {
  @NotNull
  private Long elderId;

  @NotNull
  private Long productId;

  @NotNull
  @Min(1)
  private Integer qty;
}
