package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.store.entity.InventoryBatch;
import com.zhiyangyun.care.store.entity.InventoryLog;
import com.zhiyangyun.care.store.entity.StoreOrder;
import com.zhiyangyun.care.store.mapper.InventoryBatchMapper;
import com.zhiyangyun.care.store.mapper.InventoryLogMapper;
import com.zhiyangyun.care.store.mapper.StoreOrderMapper;
import com.zhiyangyun.care.store.model.InventoryAdjustRequest;
import com.zhiyangyun.care.store.model.OrderCancelRequest;
import com.zhiyangyun.care.store.model.OrderRefundRequest;
import com.zhiyangyun.care.store.service.InventoryService;
import com.zhiyangyun.care.store.service.StoreOrderService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class InventoryFlowTest {
  @Autowired
  private StoreOrderService storeOrderService;

  @Autowired
  private StoreOrderMapper orderMapper;

  @Autowired
  private InventoryService inventoryService;

  @Autowired
  private InventoryBatchMapper batchMapper;

  @Autowired
  private InventoryLogMapper logMapper;

  @Test
  void cancel_rolls_back_inventory() {
    int before = batchMapper.selectList(Wrappers.lambdaQuery(InventoryBatch.class)
        .eq(InventoryBatch::getProductId, 6003L))
        .stream().mapToInt(batch -> batch.getQuantity() == null ? 0 : batch.getQuantity()).sum();
    var submit = new com.zhiyangyun.care.store.model.OrderPreviewRequest();
    submit.setElderId(4003L);
    submit.setProductId(6003L);
    submit.setQty(1);
    var resp = storeOrderService.submit(submit);

    OrderCancelRequest cancel = new OrderCancelRequest();
    cancel.setOrderId(resp.getOrderId());
    storeOrderService.cancel(cancel);

    long inCount = logMapper.selectCount(Wrappers.lambdaQuery(InventoryLog.class)
        .eq(InventoryLog::getRefOrderId, resp.getOrderId())
        .eq(InventoryLog::getChangeType, "IN"));
    int after = batchMapper.selectList(Wrappers.lambdaQuery(InventoryBatch.class)
        .eq(InventoryBatch::getProductId, 6003L))
        .stream().mapToInt(batch -> batch.getQuantity() == null ? 0 : batch.getQuantity()).sum();
    assertEquals(0L, inCount);
    assertEquals(before, after);
  }

  @Test
  void refund_restores_stock() {
    int before = stockOf(6003L);
    var submit = new com.zhiyangyun.care.store.model.OrderPreviewRequest();
    submit.setElderId(4003L);
    submit.setProductId(6003L);
    submit.setQty(1);
    var resp = storeOrderService.submit(submit);
    var fulfill = new com.zhiyangyun.care.store.model.OrderFulfillRequest();
    fulfill.setOrderId(resp.getOrderId());
    storeOrderService.fulfill(fulfill);
    assertEquals(before - 1, stockOf(6003L));

    OrderRefundRequest refund = new OrderRefundRequest();
    refund.setOrderId(resp.getOrderId());
    storeOrderService.refund(refund);

    // 退货回到原批次（原批次已删除时才新建 RETURN- 批次）
    assertEquals(before, stockOf(6003L));
  }

  private int stockOf(Long productId) {
    return batchMapper.selectList(Wrappers.lambdaQuery(InventoryBatch.class)
            .eq(InventoryBatch::getProductId, productId)
            .eq(InventoryBatch::getIsDeleted, 0))
        .stream().mapToInt(b -> b.getQuantity() == null ? 0 : b.getQuantity()).sum();
  }

  @Test
  void inventory_adjustment_updates_stock() {
    InventoryAdjustRequest request = new InventoryAdjustRequest();
    request.setOrgId(1L);
    request.setProductId(6003L);
    request.setAdjustType("GAIN");
    request.setAdjustQty(5);
    request.setReason("Count");

    inventoryService.adjust(request);

    int total = batchMapper.selectList(Wrappers.lambdaQuery(InventoryBatch.class)
        .eq(InventoryBatch::getProductId, 6003L))
        .stream().mapToInt(b -> b.getQuantity() == null ? 0 : b.getQuantity()).sum();
    assertTrue(total >= 5);
  }
}
