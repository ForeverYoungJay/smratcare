package com.zhiyangyun.care.store.model.material;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MaterialTransferItemRequest {
  @NotNull
  private Long productId;

  @NotNull
  private Integer quantity;
}
