package com.zhiyangyun.care.dashboard.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.dashboard.model.DashboardSummary;
import com.zhiyangyun.care.entity.CareTaskDaily;
import com.zhiyangyun.care.mapper.CareTaskDailyMapper;
import com.zhiyangyun.care.store.entity.InventoryBatch;
import com.zhiyangyun.care.store.entity.Product;
import com.zhiyangyun.care.store.mapper.InventoryBatchMapper;
import com.zhiyangyun.care.store.mapper.ProductMapper;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
  private final CareTaskDailyMapper careTaskDailyMapper;
  private final ProductMapper productMapper;
  private final InventoryBatchMapper inventoryBatchMapper;
  private final BillMonthlyMapper billMonthlyMapper;

  public DashboardController(CareTaskDailyMapper careTaskDailyMapper,
      ProductMapper productMapper,
      InventoryBatchMapper inventoryBatchMapper,
      BillMonthlyMapper billMonthlyMapper) {
    this.careTaskDailyMapper = careTaskDailyMapper;
    this.productMapper = productMapper;
    this.inventoryBatchMapper = inventoryBatchMapper;
    this.billMonthlyMapper = billMonthlyMapper;
  }

  @GetMapping("/summary")
  public Result<DashboardSummary> summary() {
    Long orgId = AuthContext.getOrgId();
    LocalDate today = LocalDate.now();

    long careTasksToday = careTaskDailyMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getTaskDate, today)
        .eq(orgId != null, CareTaskDaily::getOrgId, orgId));

    long abnormalTasksToday = careTaskDailyMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getTaskDate, today)
        .eq(orgId != null, CareTaskDaily::getOrgId, orgId)
        .eq(CareTaskDaily::getStatus, "EXCEPTION"));

    List<Product> products = productMapper.selectList(Wrappers.lambdaQuery(Product.class)
        .eq(Product::getIsDeleted, 0)
        .eq(orgId != null, Product::getOrgId, orgId));
    List<InventoryBatch> batches = inventoryBatchMapper.selectList(Wrappers.lambdaQuery(InventoryBatch.class)
        .eq(InventoryBatch::getIsDeleted, 0)
        .eq(orgId != null, InventoryBatch::getOrgId, orgId));

    Map<Long, Integer> qtyByProduct = new HashMap<>();
    for (InventoryBatch batch : batches) {
      if (batch.getProductId() == null) continue;
      qtyByProduct.merge(batch.getProductId(), batch.getQuantity() == null ? 0 : batch.getQuantity(), Integer::sum);
    }

    long inventoryAlerts = products.stream()
        .filter(p -> {
          int qty = qtyByProduct.getOrDefault(p.getId(), 0);
          int safety = p.getSafetyStock() == null ? 0 : p.getSafetyStock();
          return qty < safety;
        })
        .count();

    long unpaidBills = billMonthlyMapper.selectCount(Wrappers.lambdaQuery(BillMonthly.class)
        .eq(BillMonthly::getIsDeleted, 0)
        .eq(orgId != null, BillMonthly::getOrgId, orgId)
        .lt(BillMonthly::getStatus, 2));

    DashboardSummary summary = new DashboardSummary();
    summary.setCareTasksToday(careTasksToday);
    summary.setAbnormalTasksToday(abnormalTasksToday);
    summary.setInventoryAlerts(inventoryAlerts);
    summary.setUnpaidBills(unpaidBills);
    return Result.ok(summary);
  }
}
