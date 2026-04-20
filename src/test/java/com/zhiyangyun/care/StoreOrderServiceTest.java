package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.store.entity.InventoryLog;
import com.zhiyangyun.care.store.entity.InventoryBatch;
import com.zhiyangyun.care.store.entity.StoreOrder;
import com.zhiyangyun.care.store.entity.PointsLog;
import com.zhiyangyun.care.store.mapper.InventoryBatchMapper;
import com.zhiyangyun.care.store.mapper.StoreOrderMapper;
import com.zhiyangyun.care.store.model.OrderCancelRequest;
import com.zhiyangyun.care.store.model.OrderRefundRequest;
import com.zhiyangyun.care.store.model.OrderFulfillRequest;
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

  @Autowired
  private InventoryBatchMapper inventoryBatchMapper;

  @Autowired
  private StoreOrderMapper storeOrderMapper;

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
  void submit_creates_order_without_deducting_points_and_inventory_until_fulfill() {
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
    assertNull(pointsLog);

    InventoryLog inventoryLog = inventoryLogMapper.selectOne(
        Wrappers.lambdaQuery(InventoryLog.class).eq(InventoryLog::getRefOrderId, response.getOrderId()));
    assertNull(inventoryLog);
  }

  @Test
  void fulfill_deducts_points_and_inventory_once() {
    OrderPreviewRequest request = new OrderPreviewRequest();
    request.setElderId(4003L);
    request.setProductId(6003L);
    request.setQty(2);

    OrderSubmitResponse response = storeOrderService.submit(request);
    OrderFulfillRequest fulfillRequest = new OrderFulfillRequest();
    fulfillRequest.setOrderId(response.getOrderId());
    storeOrderService.fulfill(fulfillRequest);

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

  @Test
  void cancel_before_fulfill_does_not_restore_inventory_or_points() {
    int batchBefore = totalInventory(6003L);
    OrderSubmitResponse response = storeOrderService.submit(buildMilkRequest(1));

    OrderCancelRequest cancelRequest = new OrderCancelRequest();
    cancelRequest.setOrderId(response.getOrderId());
    storeOrderService.cancel(cancelRequest);

    assertEquals(batchBefore, totalInventory(6003L));
    assertEquals(0, countPointsLogs(response.getOrderId()));
    StoreOrder order = storeOrderMapper.selectById(response.getOrderId());
    assertEquals(4, order.getOrderStatus());
  }

  @Test
  void refund_requires_fulfilled_order_and_restores_after_fulfill() {
    OrderSubmitResponse pendingOrder = storeOrderService.submit(buildMilkRequest(1));
    OrderRefundRequest refundPending = new OrderRefundRequest();
    refundPending.setOrderId(pendingOrder.getOrderId());
    refundPending.setReason("未出库");
    assertThrows(IllegalStateException.class, () -> storeOrderService.refund(refundPending));

    int batchBefore = totalInventory(6003L);
    OrderSubmitResponse fulfilledOrder = storeOrderService.submit(buildMilkRequest(1));
    OrderFulfillRequest fulfillRequest = new OrderFulfillRequest();
    fulfillRequest.setOrderId(fulfilledOrder.getOrderId());
    storeOrderService.fulfill(fulfillRequest);
    assertEquals(batchBefore - 1, totalInventory(6003L));

    OrderRefundRequest refundRequest = new OrderRefundRequest();
    refundRequest.setOrderId(fulfilledOrder.getOrderId());
    refundRequest.setReason("退货");
    storeOrderService.refund(refundRequest);

    assertEquals(batchBefore, totalInventory(6003L));
    StoreOrder order = storeOrderMapper.selectById(fulfilledOrder.getOrderId());
    assertEquals(5, order.getOrderStatus());
  }

  private OrderPreviewRequest buildMilkRequest(int qty) {
    OrderPreviewRequest request = new OrderPreviewRequest();
    request.setElderId(4003L);
    request.setProductId(6003L);
    request.setQty(qty);
    return request;
  }

  private int totalInventory(Long productId) {
    return inventoryBatchMapper.selectList(
            Wrappers.lambdaQuery(InventoryBatch.class)
                .eq(InventoryBatch::getProductId, productId)
                .eq(InventoryBatch::getIsDeleted, 0))
        .stream()
        .map(InventoryBatch::getQuantity)
        .filter(qty -> qty != null && qty > 0)
        .mapToInt(Integer::intValue)
        .sum();
  }

  private long countPointsLogs(Long orderId) {
    return pointsLogMapper.selectCount(
        Wrappers.lambdaQuery(PointsLog.class).eq(PointsLog::getRefOrderId, orderId));
  }
}
