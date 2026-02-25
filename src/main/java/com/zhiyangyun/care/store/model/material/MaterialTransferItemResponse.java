package com.zhiyangyun.care.store.model.material;

import lombok.Data;

@Data
public class MaterialTransferItemResponse {
  private Long id;

  private Long productId;

  private String productName;

  private Integer quantity;
}
