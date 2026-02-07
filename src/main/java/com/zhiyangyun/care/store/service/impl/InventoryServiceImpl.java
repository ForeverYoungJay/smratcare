package com.zhiyangyun.care.store.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.store.entity.InventoryAdjustment;
import com.zhiyangyun.care.store.entity.InventoryBatch;
import com.zhiyangyun.care.store.entity.InventoryLog;
import com.zhiyangyun.care.store.entity.Product;
import com.zhiyangyun.care.store.mapper.InventoryAdjustmentMapper;
import com.zhiyangyun.care.store.mapper.InventoryBatchMapper;
import com.zhiyangyun.care.store.mapper.InventoryLogMapper;
import com.zhiyangyun.care.store.mapper.ProductMapper;
import com.zhiyangyun.care.store.model.InventoryAdjustRequest;
import com.zhiyangyun.care.store.model.InventoryAlertItem;
import com.zhiyangyun.care.store.model.InventoryExpiryAlertItem;
import com.zhiyangyun.care.store.service.InventoryService;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryServiceImpl implements InventoryService {
  private final InventoryBatchMapper batchMapper;
  private final InventoryAdjustmentMapper adjustmentMapper;
  private final InventoryLogMapper logMapper;
  private final ProductMapper productMapper;

  public InventoryServiceImpl(InventoryBatchMapper batchMapper,
      InventoryAdjustmentMapper adjustmentMapper,
      InventoryLogMapper logMapper,
      ProductMapper productMapper) {
    this.batchMapper = batchMapper;
    this.adjustmentMapper = adjustmentMapper;
    this.logMapper = logMapper;
    this.productMapper = productMapper;
  }

  @Override
  @Transactional
  public void adjust(InventoryAdjustRequest request) {
    InventoryAdjustment adjustment = new InventoryAdjustment();
    adjustment.setOrgId(request.getOrgId());
    adjustment.setProductId(request.getProductId());
    adjustment.setBatchId(request.getBatchId());
    adjustment.setAdjustType(request.getAdjustType());
    adjustment.setAdjustQty(request.getAdjustQty());
    adjustment.setReason(request.getReason());
    adjustment.setOperatorStaffId(request.getOperatorStaffId());
    adjustmentMapper.insert(adjustment);

    InventoryBatch batch = null;
    if (request.getBatchId() != null) {
      batch = batchMapper.selectById(request.getBatchId());
    }
    if (batch == null) {
      batch = new InventoryBatch();
      batch.setOrgId(request.getOrgId());
      batch.setProductId(request.getProductId());
      batch.setBatchNo("ADJUST-" + adjustment.getId());
      batch.setQuantity(0);
      batchMapper.insert(batch);
    }

    int current = batch.getQuantity() == null ? 0 : batch.getQuantity();
    int delta = request.getAdjustQty();
    if ("LOSS".equalsIgnoreCase(request.getAdjustType())) {
      delta = -Math.abs(delta);
    }
    batch.setQuantity(current + delta);
    batchMapper.updateById(batch);

    InventoryLog log = new InventoryLog();
    log.setOrgId(request.getOrgId());
    log.setProductId(request.getProductId());
    log.setBatchId(batch.getId());
    log.setChangeType("ADJUST");
    log.setChangeQty(delta);
    log.setRefAdjustmentId(adjustment.getId());
    log.setRemark(request.getReason());
    logMapper.insert(log);
  }

  @Override
  public List<InventoryAlertItem> lowStockAlerts(Long orgId) {
    List<Product> products = productMapper.selectList(
        Wrappers.lambdaQuery(Product.class)
            .eq(Product::getOrgId, orgId)
            .eq(Product::getIsDeleted, 0));

    Map<Long, Integer> stock = new HashMap<>();
    List<InventoryBatch> batches = batchMapper.selectList(
        Wrappers.lambdaQuery(InventoryBatch.class)
            .eq(InventoryBatch::getOrgId, orgId)
            .eq(InventoryBatch::getIsDeleted, 0));
    for (InventoryBatch batch : batches) {
      stock.merge(batch.getProductId(), batch.getQuantity() == null ? 0 : batch.getQuantity(), Integer::sum);
    }

    List<InventoryAlertItem> alerts = new ArrayList<>();
    for (Product product : products) {
      int current = stock.getOrDefault(product.getId(), 0);
      if (product.getSafetyStock() != null && current < product.getSafetyStock()) {
        InventoryAlertItem item = new InventoryAlertItem();
        item.setProductId(product.getId());
        item.setProductName(product.getProductName());
        item.setSafetyStock(product.getSafetyStock());
        item.setCurrentStock(current);
        alerts.add(item);
      }
    }
    return alerts;
  }

  @Override
  public List<InventoryExpiryAlertItem> expiryAlerts(Long orgId, int days) {
    LocalDate now = LocalDate.now();
    LocalDate threshold = now.plusDays(days);
    List<InventoryBatch> batches = batchMapper.selectList(
        Wrappers.lambdaQuery(InventoryBatch.class)
            .eq(InventoryBatch::getOrgId, orgId)
            .isNotNull(InventoryBatch::getExpireDate)
            .le(InventoryBatch::getExpireDate, threshold)
            .eq(InventoryBatch::getIsDeleted, 0));

    Map<Long, Product> productMap = new HashMap<>();
    List<InventoryExpiryAlertItem> alerts = new ArrayList<>();
    for (InventoryBatch batch : batches) {
      Product product = productMap.computeIfAbsent(batch.getProductId(),
          id -> productMapper.selectById(id));
      InventoryExpiryAlertItem item = new InventoryExpiryAlertItem();
      item.setProductId(batch.getProductId());
      item.setProductName(product == null ? null : product.getProductName());
      item.setBatchId(batch.getId());
      item.setBatchNo(batch.getBatchNo());
      item.setQuantity(batch.getQuantity());
      item.setExpireDate(batch.getExpireDate());
      item.setDaysToExpire((int) ChronoUnit.DAYS.between(now, batch.getExpireDate()));
      alerts.add(item);
    }
    return alerts;
  }
}
