package com.zhiyangyun.care.store.service;

import com.zhiyangyun.care.store.model.InventoryAdjustRequest;
import com.zhiyangyun.care.store.model.InventoryAlertItem;
import com.zhiyangyun.care.store.model.InventoryExpiryAlertItem;
import com.zhiyangyun.care.store.model.InventoryInboundRequest;
import com.zhiyangyun.care.store.model.InventoryOutboundRequest;
import java.util.List;

public interface InventoryService {
  void adjust(InventoryAdjustRequest request);

  void inbound(InventoryInboundRequest request);

  void outbound(InventoryOutboundRequest request);

  List<InventoryAlertItem> lowStockAlerts(Long orgId);

  List<InventoryExpiryAlertItem> expiryAlerts(Long orgId, int days);
}
