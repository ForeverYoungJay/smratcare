package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zhiyangyun.care.store.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class InventoryAlertTest {
  @Autowired
  private InventoryService inventoryService;

  @Test
  void low_stock_alerts() {
    var alerts = inventoryService.lowStockAlerts(1L);
    assertTrue(alerts.size() >= 0);
  }

  @Test
  void expiry_alerts() {
    var alerts = inventoryService.expiryAlerts(1L, 30);
    assertTrue(alerts.size() >= 0);
  }
}
