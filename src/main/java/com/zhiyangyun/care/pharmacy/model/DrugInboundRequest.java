package com.zhiyangyun.care.pharmacy.model;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

/** 药品入库请求。 */
@Data
public class DrugInboundRequest {
  @NotNull
  private Long drugId;
  @NotNull
  private String batchNo;
  private LocalDate expireDate;
  private LocalDate productionDate;
  @NotNull
  private Integer quantity;
  /** 进价，单位：分。 */
  private Long purchasePrice;
  private String supplier;
  private String remark;
}
