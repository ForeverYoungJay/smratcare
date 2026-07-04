package com.zhiyangyun.care.pharmacy.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 药品发药请求（FEFO 自动按批次扣减）。 */
@Data
public class DrugDispenseRequest {
  @NotNull
  private Long drugId;
  @NotNull
  private Integer quantity;
  private Long elderId;
  private Long orderId;
  private String remark;
}
