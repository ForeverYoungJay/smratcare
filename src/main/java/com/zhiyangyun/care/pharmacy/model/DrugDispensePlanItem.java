package com.zhiyangyun.care.pharmacy.model;

import lombok.Data;

/** FEFO 出库计划的一条批次分配。 */
@Data
public class DrugDispensePlanItem {
  private Long batchId;
  private String batchNo;
  private int quantity;

  public DrugDispensePlanItem() {}

  public DrugDispensePlanItem(Long batchId, String batchNo, int quantity) {
    this.batchId = batchId;
    this.batchNo = batchNo;
    this.quantity = quantity;
  }
}
