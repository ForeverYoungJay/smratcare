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
import com.zhiyangyun.care.store.model.InventoryInboundRequest;
import com.zhiyangyun.care.store.model.InventoryOutboundRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
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
    log.setBizType("ADJUST");
    fillProductSnapshot(log, request.getOrgId(), request.getProductId());
    log.setRemark(request.getReason());
    logMapper.insert(log);
  }

  @Override
  @Transactional
  public void inbound(InventoryInboundRequest request) {
    String batchNo = request.getBatchNo();
    if (batchNo == null || batchNo.isBlank()) {
      String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
      int rand = new Random().nextInt(9000) + 1000;
      batchNo = "IN-" + date + "-" + rand;
    }
    InventoryBatch batch = batchMapper.selectOne(
        Wrappers.lambdaQuery(InventoryBatch.class)
            .eq(InventoryBatch::getOrgId, request.getOrgId())
            .eq(InventoryBatch::getProductId, request.getProductId())
            .eq(InventoryBatch::getBatchNo, batchNo)
            .eq(InventoryBatch::getIsDeleted, 0));
    if (batch == null) {
      batch = new InventoryBatch();
      batch.setOrgId(request.getOrgId());
      batch.setProductId(request.getProductId());
      batch.setBatchNo(batchNo);
      batch.setQuantity(request.getQuantity());
      batch.setCostPrice(request.getCostPrice() == null ? BigDecimal.ZERO : request.getCostPrice());
      batch.setExpireDate(request.getExpireDate());
      batch.setWarehouseLocation(request.getWarehouseLocation());
      batchMapper.insert(batch);
    } else {
      int current = batch.getQuantity() == null ? 0 : batch.getQuantity();
      batch.setQuantity(current + request.getQuantity());
      if (request.getCostPrice() != null) {
        batch.setCostPrice(request.getCostPrice());
      }
      if (request.getExpireDate() != null) {
        batch.setExpireDate(request.getExpireDate());
      }
      if (request.getWarehouseLocation() != null) {
        batch.setWarehouseLocation(request.getWarehouseLocation());
      }
      batchMapper.updateById(batch);
    }

    InventoryLog log = new InventoryLog();
    log.setOrgId(request.getOrgId());
    log.setProductId(request.getProductId());
    log.setBatchId(batch.getId());
    log.setChangeType("IN");
    log.setChangeQty(request.getQuantity());
    log.setBizType("INBOUND");
    fillProductSnapshot(log, request.getOrgId(), request.getProductId());
    log.setRemark(request.getRemark());
    logMapper.insert(log);
  }

  @Override
  @Transactional
  public void outbound(InventoryOutboundRequest request) {
    if (request.getQuantity() == null || request.getQuantity() <= 0) {
      throw new IllegalArgumentException("quantity must be positive");
    }
    if (request.getBatchId() != null) {
      InventoryBatch batch = batchMapper.selectById(request.getBatchId());
      if (batch == null) {
        throw new IllegalArgumentException("batch not found");
      }
      if (request.getOrgId() != null && !request.getOrgId().equals(batch.getOrgId())) {
        throw new IllegalStateException("org mismatch");
      }
      int current = batch.getQuantity() == null ? 0 : batch.getQuantity();
      if (current < request.getQuantity()) {
        throw new IllegalStateException("insufficient stock");
      }
      batch.setQuantity(current - request.getQuantity());
      batchMapper.updateById(batch);

      InventoryLog log = new InventoryLog();
      log.setOrgId(request.getOrgId());
      log.setProductId(request.getProductId());
      log.setBatchId(batch.getId());
      log.setChangeType("OUT");
      log.setChangeQty(request.getQuantity());
      log.setBizType("CONSUME");
      fillProductSnapshot(log, request.getOrgId(), request.getProductId());
      log.setRemark(request.getReason());
      logMapper.insert(log);
      return;
    }

    int remaining = request.getQuantity();
    List<InventoryBatch> batches = batchMapper.selectList(
        Wrappers.lambdaQuery(InventoryBatch.class)
            .eq(InventoryBatch::getOrgId, request.getOrgId())
            .eq(InventoryBatch::getProductId, request.getProductId())
            .orderByAsc(InventoryBatch::getExpireDate)
            .orderByAsc(InventoryBatch::getId));
    int total = 0;
    for (InventoryBatch batch : batches) {
      total += batch.getQuantity() == null ? 0 : batch.getQuantity();
    }
    if (total < remaining) {
      throw new IllegalStateException("insufficient stock");
    }
    for (InventoryBatch batch : batches) {
      if (remaining <= 0) {
        break;
      }
      int available = batch.getQuantity() == null ? 0 : batch.getQuantity();
      if (available <= 0) {
        continue;
      }
      int used = Math.min(available, remaining);
      batch.setQuantity(available - used);
      batchMapper.updateById(batch);

      InventoryLog log = new InventoryLog();
      log.setOrgId(request.getOrgId());
      log.setProductId(request.getProductId());
      log.setBatchId(batch.getId());
      log.setChangeType("OUT");
      log.setChangeQty(used);
      log.setBizType("CONSUME");
      fillProductSnapshot(log, request.getOrgId(), request.getProductId());
      log.setRemark(request.getReason());
      logMapper.insert(log);
      remaining -= used;
    }
  }

  private void fillProductSnapshot(InventoryLog log, Long orgId, Long productId) {
    if (productId == null) {
      return;
    }
    Product product = productMapper.selectById(productId);
    if (product == null) {
      return;
    }
    if (orgId != null && product.getOrgId() != null && !orgId.equals(product.getOrgId())) {
      return;
    }
    log.setProductCodeSnapshot(product.getProductCode());
    log.setProductNameSnapshot(product.getProductName());
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
