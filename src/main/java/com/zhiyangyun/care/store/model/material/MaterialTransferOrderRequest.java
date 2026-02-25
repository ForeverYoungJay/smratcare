package com.zhiyangyun.care.store.model.material;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class MaterialTransferOrderRequest {
  @NotNull
  private Long fromWarehouseId;

  @NotNull
  private Long toWarehouseId;

  private String remark;

  @Valid
  @NotEmpty
  private List<MaterialTransferItemRequest> items;
}
