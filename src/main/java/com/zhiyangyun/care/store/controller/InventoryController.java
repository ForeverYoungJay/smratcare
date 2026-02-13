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
import com.zhiyangyun.care.store.entity.Product;
import com.zhiyangyun.care.store.mapper.InventoryBatchMapper;
import com.zhiyangyun.care.store.mapper.InventoryAdjustmentMapper;
import com.zhiyangyun.care.store.mapper.InventoryLogMapper;
import com.zhiyangyun.care.store.mapper.ProductMapper;
import com.zhiyangyun.care.store.model.InventoryAdjustRequest;
import com.zhiyangyun.care.store.model.InventoryAlertItem;
import com.zhiyangyun.care.store.model.InventoryBatchResponse;
import com.zhiyangyun.care.store.model.InventoryExpiryAlertItem;
import com.zhiyangyun.care.store.model.InventoryInboundRequest;
import com.zhiyangyun.care.store.model.InventoryOutboundRequest;
import com.zhiyangyun.care.store.model.InventoryAdjustmentResponse;
import com.zhiyangyun.care.store.model.InventoryLogResponse;
import com.zhiyangyun.care.store.service.InventoryService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
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
  private final AuditLogService auditLogService;

  public InventoryController(InventoryService inventoryService,
      InventoryBatchMapper inventoryBatchMapper,
      InventoryAdjustmentMapper inventoryAdjustmentMapper,
      InventoryLogMapper inventoryLogMapper,
      ProductMapper productMapper,
      AuditLogService auditLogService) {
    this.inventoryService = inventoryService;
    this.inventoryBatchMapper = inventoryBatchMapper;
    this.inventoryAdjustmentMapper = inventoryAdjustmentMapper;
    this.inventoryLogMapper = inventoryLogMapper;
    this.productMapper = productMapper;
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
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    LambdaQueryWrapper<InventoryBatch> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(orgId != null, InventoryBatch::getOrgId, orgId)
        .eq(InventoryBatch::getIsDeleted, 0)
        .eq(productId != null, InventoryBatch::getProductId, productId);
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
    IPage<InventoryBatchResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(batch -> {
      InventoryBatchResponse item = new InventoryBatchResponse();
      Product product = productMap.get(batch.getProductId());
      item.setId(batch.getId());
      item.setProductId(batch.getProductId());
      item.setProductName(product == null ? null : product.getProductName());
      item.setSafetyStock(product == null ? null : product.getSafetyStock());
      item.setBatchNo(batch.getBatchNo());
      item.setQuantity(batch.getQuantity());
      item.setCostPrice(batch.getCostPrice());
      item.setExpireDate(batch.getExpireDate());
      item.setWarehouseLocation(batch.getWarehouseLocation());
      item.setCreateTime(batch.getCreateTime());
      return item;
    }).toList());
    return Result.ok(resp);
  }

  @GetMapping("/log/page")
  public Result<IPage<InventoryLogResponse>> logPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long productId,
      @RequestParam(required = false) String changeType,
      @RequestParam(required = false) String outType,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo) {
    Long orgId = AuthContext.getOrgId();
    LambdaQueryWrapper<InventoryLog> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(orgId != null, InventoryLog::getOrgId, orgId)
        .eq(InventoryLog::getIsDeleted, 0)
        .eq(productId != null, InventoryLog::getProductId, productId)
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
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo) {
    return logPage(pageNo, pageSize, productId, "IN", null, dateFrom, dateTo);
  }

  @GetMapping("/outbound/page")
  public Result<IPage<InventoryLogResponse>> outboundPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long productId,
      @RequestParam(required = false) String outType,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo) {
    return logPage(pageNo, pageSize, productId, "OUT", outType, dateFrom, dateTo);
  }

  @GetMapping("/adjustment/page")
  public Result<IPage<InventoryAdjustmentResponse>> adjustmentPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long productId,
      @RequestParam(required = false) String adjustType) {
    Long orgId = AuthContext.getOrgId();
    LambdaQueryWrapper<InventoryAdjustment> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(orgId != null, InventoryAdjustment::getOrgId, orgId)
        .eq(InventoryAdjustment::getIsDeleted, 0)
        .eq(productId != null, InventoryAdjustment::getProductId, productId)
        .eq(adjustType != null && !adjustType.isBlank(), InventoryAdjustment::getAdjustType, adjustType)
        .orderByDesc(InventoryAdjustment::getCreateTime);
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
    IPage<InventoryAdjustmentResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(adjust -> {
      InventoryAdjustmentResponse item = new InventoryAdjustmentResponse();
      Product product = productMap.get(adjust.getProductId());
      item.setId(adjust.getId());
      item.setProductId(adjust.getProductId());
      item.setProductName(product == null ? null : product.getProductName());
      item.setBatchId(adjust.getBatchId());
      item.setAdjustType(adjust.getAdjustType());
      item.setAdjustQty(adjust.getAdjustQty());
      item.setReason(adjust.getReason());
      item.setOperatorStaffId(adjust.getOperatorStaffId());
      item.setCreateTime(adjust.getCreateTime());
      return item;
    }).toList());
    return Result.ok(resp);
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
