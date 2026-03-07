package com.zhiyangyun.care.store.model;

import lombok.Data;

@Data
public class InventoryOutboundSheetPrefillResponse {
  private Long elderId;
  private String receiverName;
  private String contractNo;
  private String applyDept;
}
