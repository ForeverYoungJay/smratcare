package com.zhiyangyun.care.store.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.store.entity.InventoryBatch;
import com.zhiyangyun.care.store.entity.InventoryLog;
import com.zhiyangyun.care.store.entity.InventoryAdjustment;
import com.zhiyangyun.care.store.entity.MaterialWarehouse;
import com.zhiyangyun.care.store.entity.Product;
import com.zhiyangyun.care.store.mapper.InventoryBatchMapper;
import com.zhiyangyun.care.store.mapper.InventoryAdjustmentMapper;
import com.zhiyangyun.care.store.mapper.InventoryLogMapper;
import com.zhiyangyun.care.store.mapper.MaterialWarehouseMapper;
import com.zhiyangyun.care.store.mapper.ProductMapper;
import com.zhiyangyun.care.store.model.InventoryAdjustRequest;
import com.zhiyangyun.care.store.model.InventoryAlertItem;
import com.zhiyangyun.care.store.model.InventoryBatchResponse;
import com.zhiyangyun.care.store.model.InventoryExpiryAlertItem;
import com.zhiyangyun.care.store.model.InventoryInboundRequest;
import com.zhiyangyun.care.store.model.InventoryOutboundRequest;
import com.zhiyangyun.care.store.model.InventoryAdjustmentResponse;
import com.zhiyangyun.care.store.model.InventoryAdjustmentDiffItem;
import com.zhiyangyun.care.store.model.InventoryLogResponse;
import com.zhiyangyun.care.store.service.InventoryService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
  private final InventoryService inventoryService;
  private final InventoryBatchMapper inventoryBatchMapper;
  private final InventoryAdjustmentMapper inventoryAdjustmentMapper;
  private final InventoryLogMapper inventoryLogMapper;
  private final ProductMapper productMapper;
  private final MaterialWarehouseMapper materialWarehouseMapper;
  private final AuditLogService auditLogService;

  public InventoryController(InventoryService inventoryService,
      InventoryBatchMapper inventoryBatchMapper,
      InventoryAdjustmentMapper inventoryAdjustmentMapper,
      InventoryLogMapper inventoryLogMapper,
      ProductMapper productMapper,
      MaterialWarehouseMapper materialWarehouseMapper,
      AuditLogService auditLogService) {
    this.inventoryService = inventoryService;
    this.inventoryBatchMapper = inventoryBatchMapper;
    this.inventoryAdjustmentMapper = inventoryAdjustmentMapper;
    this.inventoryLogMapper = inventoryLogMapper;
    this.productMapper = productMapper;
    this.materialWarehouseMapper = materialWarehouseMapper;
    this.auditLogService = auditLogService;
  }

  @PostMapping("/adjust")
  public Result<Void> adjust(@Valid @RequestBody InventoryAdjustRequest request) {
    Long orgId = AuthContext.getOrgId();
    request.setOrgId(orgId);
    request.setOperatorStaffId(AuthContext.getStaffId());
    inventoryService.adjust(request);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "INVENTORY_ADJUST", "PRODUCT", request.getProductId(), request.getReason());
    return Result.ok(null);
  }

  @PostMapping("/inbound")
  public Result<Void> inbound(@Valid @RequestBody InventoryInboundRequest request) {
    Long orgId = AuthContext.getOrgId();
    request.setOrgId(orgId);
    inventoryService.inbound(request);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "INVENTORY_IN", "PRODUCT", request.getProductId(), request.getRemark());
    return Result.ok(null);
  }

  @PostMapping("/outbound")
  public Result<Void> outbound(@Valid @RequestBody InventoryOutboundRequest request) {
    Long orgId = AuthContext.getOrgId();
    request.setOrgId(orgId);
    request.setOperatorStaffId(AuthContext.getStaffId());
    inventoryService.outbound(request);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "INVENTORY_OUT", "PRODUCT", request.getProductId(), request.getReason());
    return Result.ok(null);
  }

  @GetMapping("/alerts")
  public Result<List<InventoryAlertItem>> alerts() {
    return Result.ok(inventoryService.lowStockAlerts(AuthContext.getOrgId()));
  }

  @GetMapping("/expiry-alerts")
  public Result<List<InventoryExpiryAlertItem>> expiryAlerts(
      @RequestParam(defaultValue = "30") int days) {
    return Result.ok(inventoryService.expiryAlerts(AuthContext.getOrgId(), days));
  }

  @GetMapping("/batch/page")
  public Result<IPage<InventoryBatchResponse>> batchPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long productId,
      @RequestParam(required = false) Long warehouseId,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "false") boolean lowStockOnly,
      @RequestParam(required = false) Integer expiryDays) {
    Long orgId = AuthContext.getOrgId();
    LambdaQueryWrapper<InventoryBatch> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(orgId != null, InventoryBatch::getOrgId, orgId)
        .eq(InventoryBatch::getIsDeleted, 0)
        .eq(productId != null, InventoryBatch::getProductId, productId)
        .eq(warehouseId != null, InventoryBatch::getWarehouseId, warehouseId);
    if (category != null && !category.isBlank()) {
      List<Long> categoryProductIds = productMapper.selectList(
          Wrappers.lambdaQuery(Product.class)
              .eq(Product::getIsDeleted, 0)
              .eq(orgId != null, Product::getOrgId, orgId)
              .like(Product::getCategory, category))
          .stream()
          .map(Product::getId)
          .toList();
      if (categoryProductIds.isEmpty()) {
        return Result.ok(new Page<>(pageNo, pageSize, 0));
      }
      wrapper.in(InventoryBatch::getProductId, categoryProductIds);
    }
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(InventoryBatch::getBatchNo, keyword));
      List<Product> products = productMapper.selectList(
          Wrappers.lambdaQuery(Product.class)
              .eq(Product::getIsDeleted, 0)
              .eq(orgId != null, Product::getOrgId, orgId)
              .and(p -> p.like(Product::getProductName, keyword)
                  .or().like(Product::getProductCode, keyword)));
      if (!products.isEmpty()) {
        wrapper.or(w -> w.in(InventoryBatch::getProductId,
            products.stream().map(Product::getId).toList()));
      }
    }
    wrapper.orderByDesc(InventoryBatch::getCreateTime);
    IPage<InventoryBatch> page = inventoryBatchMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    List<Long> batchProductIds = page.getRecords().stream()
        .map(InventoryBatch::getProductId)
        .filter(java.util.Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, Product> productMap = batchProductIds.isEmpty()
        ? Map.of()
        : productMapper.selectBatchIds(batchProductIds)
            .stream()
            .collect(Collectors.toMap(Product::getId, p -> p));
    Map<Long, MaterialWarehouse> warehouseMap = page.getRecords().stream()
        .map(InventoryBatch::getWarehouseId)
        .filter(java.util.Objects::nonNull)
        .distinct()
        .collect(Collectors.toMap(
            id -> id,
            id -> materialWarehouseMapper.selectById(id),
            (a, b) -> a));
    IPage<InventoryBatchResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    List<InventoryBatchResponse> records = page.getRecords().stream().map(batch -> {
      InventoryBatchResponse item = new InventoryBatchResponse();
      Product product = productMap.get(batch.getProductId());
      MaterialWarehouse warehouse =
          batch.getWarehouseId() == null ? null : warehouseMap.get(batch.getWarehouseId());
      item.setId(batch.getId());
      item.setProductId(batch.getProductId());
      item.setProductName(product == null ? null : product.getProductName());
      item.setCategory(product == null ? null : product.getCategory());
      item.setSafetyStock(product == null ? null : product.getSafetyStock());
      item.setWarehouseId(batch.getWarehouseId());
      item.setWarehouseName(warehouse == null ? null : warehouse.getWarehouseName());
      item.setBatchNo(batch.getBatchNo());
      item.setQuantity(batch.getQuantity());
      item.setCostPrice(batch.getCostPrice());
      item.setExpireDate(batch.getExpireDate());
      item.setWarehouseLocation(batch.getWarehouseLocation());
      item.setCreateTime(batch.getCreateTime());
      return item;
    }).toList();
    if (lowStockOnly) {
      records = records.stream()
          .filter(row -> row.getSafetyStock() != null && row.getQuantity() != null && row.getQuantity() < row.getSafetyStock())
          .toList();
    }
    if (expiryDays != null && expiryDays >= 0) {
      LocalDate threshold = LocalDate.now().plusDays(expiryDays);
      records = records.stream()
          .filter(row -> row.getExpireDate() != null && !row.getExpireDate().isAfter(threshold))
          .toList();
    }
    resp.setRecords(records);
    return Result.ok(resp);
  }

  @GetMapping("/log/page")
  public Result<IPage<InventoryLogResponse>> logPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long productId,
      @RequestParam(required = false) Long warehouseId,
      @RequestParam(required = false) String changeType,
      @RequestParam(required = false) String outType,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo) {
    Long orgId = AuthContext.getOrgId();
    LambdaQueryWrapper<InventoryLog> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(orgId != null, InventoryLog::getOrgId, orgId)
        .eq(InventoryLog::getIsDeleted, 0)
        .eq(productId != null, InventoryLog::getProductId, productId)
        .eq(warehouseId != null, InventoryLog::getWarehouseId, warehouseId)
        .eq(changeType != null && !changeType.isBlank(), InventoryLog::getChangeType, changeType);
    if (outType != null && !outType.isBlank()) {
      if ("SALE".equalsIgnoreCase(outType)) {
        wrapper.isNotNull(InventoryLog::getRefOrderId);
      } else if ("CONSUME".equalsIgnoreCase(outType)) {
        wrapper.isNull(InventoryLog::getRefOrderId);
      }
    }
    if (dateFrom != null && !dateFrom.isBlank()) {
      LocalDate start = LocalDate.parse(dateFrom);
      wrapper.ge(InventoryLog::getCreateTime, LocalDateTime.of(start, LocalTime.MIN));
    }
    if (dateTo != null && !dateTo.isBlank()) {
      LocalDate end = LocalDate.parse(dateTo);
      wrapper.le(InventoryLog::getCreateTime, LocalDateTime.of(end, LocalTime.MAX));
    }
    wrapper.orderByDesc(InventoryLog::getCreateTime);
    IPage<InventoryLog> page = inventoryLogMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    List<Long> productIds = page.getRecords().stream()
        .map(InventoryLog::getProductId)
        .filter(java.util.Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, Product> productMap = productIds.isEmpty()
        ? Map.of()
        : productMapper.selectBatchIds(productIds)
            .stream()
            .collect(Collectors.toMap(Product::getId, p -> p));
    List<Long> batchIds = page.getRecords().stream()
        .map(InventoryLog::getBatchId)
        .filter(java.util.Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, InventoryBatch> batchMap = batchIds.isEmpty()
        ? Map.of()
        : inventoryBatchMapper.selectBatchIds(batchIds)
            .stream()
            .collect(Collectors.toMap(InventoryBatch::getId, b -> b));
    IPage<InventoryLogResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(log -> {
      InventoryLogResponse item = new InventoryLogResponse();
      InventoryBatch batch = batchMap.get(log.getBatchId());
      Product product = productMap.get(log.getProductId());
      item.setId(log.getId());
      item.setProductId(log.getProductId());
      String snapshotName = log.getProductNameSnapshot();
      item.setProductName(snapshotName != null ? snapshotName : (product == null ? null : product.getProductName()));
      item.setBatchId(log.getBatchId());
      item.setWarehouseId(log.getWarehouseId());
      item.setBatchNo(batch == null ? null : batch.getBatchNo());
      item.setChangeType(log.getChangeType());
      item.setChangeQty(log.getChangeQty());
      item.setRefOrderId(log.getRefOrderId());
      item.setRefAdjustmentId(log.getRefAdjustmentId());
      if ("OUT".equalsIgnoreCase(log.getChangeType())) {
        item.setOutType(log.getRefOrderId() != null ? "SALE" : "CONSUME");
      }
      item.setRemark(log.getRemark());
      item.setCreateTime(log.getCreateTime());
      return item;
    }).toList());
    return Result.ok(resp);
  }

  @GetMapping("/inbound/page")
  public Result<IPage<InventoryLogResponse>> inboundPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long productId,
      @RequestParam(required = false) Long warehouseId,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo) {
    return logPage(pageNo, pageSize, productId, warehouseId, "IN", null, dateFrom, dateTo);
  }

  @GetMapping("/outbound/page")
  public Result<IPage<InventoryLogResponse>> outboundPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long productId,
      @RequestParam(required = false) Long warehouseId,
      @RequestParam(required = false) String outType,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo) {
    return logPage(pageNo, pageSize, productId, warehouseId, "OUT", outType, dateFrom, dateTo);
  }

  @GetMapping("/adjustment/page")
  public Result<IPage<InventoryAdjustmentResponse>> adjustmentPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long productId,
      @RequestParam(required = false) Long warehouseId,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String adjustType,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo) {
    Long orgId = AuthContext.getOrgId();
    LambdaQueryWrapper<InventoryAdjustment> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(orgId != null, InventoryAdjustment::getOrgId, orgId)
        .eq(InventoryAdjustment::getIsDeleted, 0)
        .eq(productId != null, InventoryAdjustment::getProductId, productId)
        .eq(adjustType != null && !adjustType.isBlank(), InventoryAdjustment::getAdjustType, adjustType);
    if (dateFrom != null && !dateFrom.isBlank()) {
      LocalDate start = LocalDate.parse(dateFrom);
      wrapper.ge(InventoryAdjustment::getCreateTime, LocalDateTime.of(start, LocalTime.MIN));
    }
    if (dateTo != null && !dateTo.isBlank()) {
      LocalDate end = LocalDate.parse(dateTo);
      wrapper.le(InventoryAdjustment::getCreateTime, LocalDateTime.of(end, LocalTime.MAX));
    }
    wrapper.orderByDesc(InventoryAdjustment::getCreateTime);
    IPage<InventoryAdjustment> page =
        inventoryAdjustmentMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    List<Long> productIds = page.getRecords().stream()
        .map(InventoryAdjustment::getProductId)
        .filter(java.util.Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, Product> productMap = productIds.isEmpty()
        ? Map.of()
        : productMapper.selectBatchIds(productIds)
            .stream()
            .collect(Collectors.toMap(Product::getId, p -> p));
    List<Long> batchIds = page.getRecords().stream()
        .map(InventoryAdjustment::getBatchId)
        .filter(java.util.Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, InventoryBatch> batchMap = batchIds.isEmpty()
        ? Map.of()
        : inventoryBatchMapper.selectBatchIds(batchIds)
            .stream()
            .collect(Collectors.toMap(InventoryBatch::getId, b -> b));
    Map<Long, MaterialWarehouse> warehouseMap = batchMap.values().stream()
        .map(InventoryBatch::getWarehouseId)
        .filter(java.util.Objects::nonNull)
        .distinct()
        .collect(Collectors.toMap(
            id -> id,
            id -> materialWarehouseMapper.selectById(id),
            (a, b) -> a));
    IPage<InventoryAdjustmentResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    List<InventoryAdjustmentResponse> records = page.getRecords().stream().map(adjust -> {
      InventoryAdjustmentResponse item = new InventoryAdjustmentResponse();
      Product product = productMap.get(adjust.getProductId());
      InventoryBatch batch = adjust.getBatchId() == null ? null : batchMap.get(adjust.getBatchId());
      MaterialWarehouse warehouse = batch == null || batch.getWarehouseId() == null
          ? null : warehouseMap.get(batch.getWarehouseId());
      item.setId(adjust.getId());
      item.setProductId(adjust.getProductId());
      item.setProductName(product == null ? null : product.getProductName());
      item.setCategory(product == null ? null : product.getCategory());
      item.setBatchId(adjust.getBatchId());
      item.setWarehouseId(batch == null ? null : batch.getWarehouseId());
      item.setWarehouseName(warehouse == null ? null : warehouse.getWarehouseName());
      item.setAdjustType(adjust.getAdjustType());
      item.setAdjustQty(adjust.getAdjustQty());
      item.setReason(adjust.getReason());
      item.setOperatorStaffId(adjust.getOperatorStaffId());
      item.setCreateTime(adjust.getCreateTime());
      return item;
    }).toList();
    if (warehouseId != null) {
      records = records.stream()
          .filter(row -> warehouseId.equals(row.getWarehouseId()))
          .toList();
    }
    if (category != null && !category.isBlank()) {
      records = records.stream()
          .filter(row -> row.getCategory() != null && row.getCategory().contains(category))
          .toList();
    }
    resp.setRecords(records);
    return Result.ok(resp);
  }

  @GetMapping("/adjustment/diff-report")
  public Result<List<InventoryAdjustmentDiffItem>> adjustmentDiffReport(
      @RequestParam(required = false) Long warehouseId,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo) {
    Long orgId = AuthContext.getOrgId();
    LambdaQueryWrapper<InventoryAdjustment> wrapper = Wrappers.lambdaQuery(InventoryAdjustment.class)
        .eq(InventoryAdjustment::getIsDeleted, 0)
        .eq(orgId != null, InventoryAdjustment::getOrgId, orgId);
    if (dateFrom != null && !dateFrom.isBlank()) {
      LocalDate start = LocalDate.parse(dateFrom);
      wrapper.ge(InventoryAdjustment::getCreateTime, LocalDateTime.of(start, LocalTime.MIN));
    }
    if (dateTo != null && !dateTo.isBlank()) {
      LocalDate end = LocalDate.parse(dateTo);
      wrapper.le(InventoryAdjustment::getCreateTime, LocalDateTime.of(end, LocalTime.MAX));
    }
    List<InventoryAdjustment> adjustments = inventoryAdjustmentMapper.selectList(wrapper);
    if (adjustments.isEmpty()) {
      return Result.ok(List.of());
    }
    List<Long> productIds = adjustments.stream()
        .map(InventoryAdjustment::getProductId)
        .filter(java.util.Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, Product> productMap = productIds.isEmpty()
        ? Map.of()
        : productMapper.selectBatchIds(productIds).stream()
            .collect(Collectors.toMap(Product::getId, p -> p));
    List<Long> batchIds = adjustments.stream()
        .map(InventoryAdjustment::getBatchId)
        .filter(java.util.Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, InventoryBatch> batchMap = batchIds.isEmpty()
        ? Map.of()
        : inventoryBatchMapper.selectBatchIds(batchIds).stream()
            .collect(Collectors.toMap(InventoryBatch::getId, b -> b));
    Map<Long, MaterialWarehouse> warehouseMap = batchMap.values().stream()
        .map(InventoryBatch::getWarehouseId)
        .filter(java.util.Objects::nonNull)
        .distinct()
        .collect(Collectors.toMap(
            id -> id,
            id -> materialWarehouseMapper.selectById(id),
            (a, b) -> a));

    Map<String, InventoryAdjustmentDiffItem> resultMap = new HashMap<>();
    for (InventoryAdjustment row : adjustments) {
      Product product = productMap.get(row.getProductId());
      InventoryBatch batch = row.getBatchId() == null ? null : batchMap.get(row.getBatchId());
      Long rowWarehouseId = batch == null ? null : batch.getWarehouseId();
      String rowCategory = product == null ? null : product.getCategory();
      if (warehouseId != null && !warehouseId.equals(rowWarehouseId)) {
        continue;
      }
      if (category != null && !category.isBlank() && (rowCategory == null || !rowCategory.contains(category))) {
        continue;
      }
      String key = row.getProductId() + "-" + (rowWarehouseId == null ? "0" : rowWarehouseId);
      InventoryAdjustmentDiffItem item = resultMap.computeIfAbsent(key, k -> {
        InventoryAdjustmentDiffItem it = new InventoryAdjustmentDiffItem();
        it.setProductId(row.getProductId());
        it.setProductName(product == null ? null : product.getProductName());
        it.setCategory(rowCategory);
        it.setWarehouseId(rowWarehouseId);
        MaterialWarehouse warehouse = rowWarehouseId == null ? null : warehouseMap.get(rowWarehouseId);
        it.setWarehouseName(warehouse == null ? null : warehouse.getWarehouseName());
        it.setGainQty(0);
        it.setLossQty(0);
        it.setDiffQty(0);
        return it;
      });
      int qty = row.getAdjustQty() == null ? 0 : row.getAdjustQty();
      if ("GAIN".equalsIgnoreCase(row.getAdjustType())) {
        item.setGainQty(item.getGainQty() + qty);
        item.setDiffQty(item.getDiffQty() + qty);
      } else {
        item.setLossQty(item.getLossQty() + qty);
        item.setDiffQty(item.getDiffQty() - qty);
      }
    }
    return Result.ok(resultMap.values().stream()
        .sorted((a, b) -> Integer.compare(Math.abs(b.getDiffQty()), Math.abs(a.getDiffQty())))
        .toList());
  }

  @GetMapping("/page")
  public Result<IPage<InventoryBatch>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long productId) {
    Long orgId = AuthContext.getOrgId();
    LambdaQueryWrapper<InventoryBatch> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(orgId != null, InventoryBatch::getOrgId, orgId)
        .eq(InventoryBatch::getIsDeleted, 0)
        .eq(productId != null, InventoryBatch::getProductId, productId)
        .orderByDesc(InventoryBatch::getCreateTime);
    return Result.ok(inventoryBatchMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }
}
