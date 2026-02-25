package com.zhiyangyun.care.store.model.material;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class MaterialTransferOrderDetailResponse {
  private Long id;

  private String transferNo;

  private Long fromWarehouseId;

  private String fromWarehouseName;

  private Long toWarehouseId;

  private String toWarehouseName;

  private String status;

  private String remark;

  private LocalDateTime createTime;

  private List<MaterialTransferItemResponse> items;
}
