package com.zhiyangyun.care.store.model;

import lombok.Data;

@Data
public class MaterialCenterOverviewResponse {
  private long lowStockCount;
  private long expiryAlertCount;
  private long materialPurchaseDraftCount;
  private long materialTransferDraftCount;
}
