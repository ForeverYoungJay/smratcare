package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.store.entity.InventoryLog;
import com.zhiyangyun.care.store.entity.PointsLog;
import com.zhiyangyun.care.store.model.OrderPreviewRequest;
import com.zhiyangyun.care.store.model.OrderPreviewResponse;
import com.zhiyangyun.care.store.model.OrderStatus;
import com.zhiyangyun.care.store.model.OrderSubmitResponse;
import com.zhiyangyun.care.store.service.StoreOrderService;
import com.zhiyangyun.care.store.mapper.InventoryLogMapper;
import com.zhiyangyun.care.store.mapper.PointsLogMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class StoreOrderServiceTest {
  @Autowired
  private StoreOrderService storeOrderService;

  @Autowired
  private PointsLogMapper pointsLogMapper;

  @Autowired
  private InventoryLogMapper inventoryLogMapper;

  @Test
  void preview_blocks_forbidden_tag() {
    OrderPreviewRequest request = new OrderPreviewRequest();
    request.setElderId(4001L);
    request.setProductId(6001L);
    request.setQty(1);

    OrderPreviewResponse response = storeOrderService.preview(request);
    assertEquals(OrderStatus.BLOCKED.name(), response.getStatus());
    assertFalse(response.isAllowed());
    assertFalse(response.getReasons().isEmpty());
  }

  @Test
  void submit_creates_order_and_deducts_points_and_inventory() {
    OrderPreviewRequest request = new OrderPreviewRequest();
    request.setElderId(4003L);
    request.setProductId(6003L);
    request.setQty(2);

    OrderSubmitResponse response = storeOrderService.submit(request);
    assertEquals(OrderStatus.ALLOWED.name(), response.getStatus());
    assertTrue(response.isAllowed());
    assertNotNull(response.getOrderId());

    PointsLog pointsLog = pointsLogMapper.selectOne(
        Wrappers.lambdaQuery(PointsLog.class).eq(PointsLog::getRefOrderId, response.getOrderId()));
    assertNotNull(pointsLog);
    assertEquals(10, pointsLog.getChangePoints());

    InventoryLog inventoryLog = inventoryLogMapper.selectOne(
        Wrappers.lambdaQuery(InventoryLog.class).eq(InventoryLog::getRefOrderId, response.getOrderId()));
    assertNotNull(inventoryLog);
    assertEquals("OUT", inventoryLog.getChangeType());
    assertEquals(2, inventoryLog.getChangeQty());
  }
}
