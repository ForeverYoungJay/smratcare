package com.zhiyangyun.care.store.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.store.entity.InventoryBatch;
import com.zhiyangyun.care.store.entity.InventoryLog;
import com.zhiyangyun.care.store.entity.MaterialPurchaseOrder;
import com.zhiyangyun.care.store.entity.MaterialPurchaseOrderItem;
import com.zhiyangyun.care.store.entity.MaterialSupplier;
import com.zhiyangyun.care.store.entity.MaterialTransferItem;
import com.zhiyangyun.care.store.entity.MaterialTransferOrder;
import com.zhiyangyun.care.store.entity.MaterialWarehouse;
import com.zhiyangyun.care.store.entity.Product;
import com.zhiyangyun.care.store.mapper.InventoryBatchMapper;
import com.zhiyangyun.care.store.mapper.InventoryLogMapper;
import com.zhiyangyun.care.store.mapper.MaterialPurchaseOrderItemMapper;
import com.zhiyangyun.care.store.mapper.MaterialPurchaseOrderMapper;
import com.zhiyangyun.care.store.mapper.MaterialSupplierMapper;
import com.zhiyangyun.care.store.mapper.MaterialTransferItemMapper;
import com.zhiyangyun.care.store.mapper.MaterialTransferOrderMapper;
import com.zhiyangyun.care.store.mapper.MaterialWarehouseMapper;
import com.zhiyangyun.care.store.mapper.ProductMapper;
import com.zhiyangyun.care.store.model.material.MaterialPurchaseOrderDetailResponse;
import com.zhiyangyun.care.store.model.material.MaterialPurchaseOrderItemRequest;
import com.zhiyangyun.care.store.model.material.MaterialPurchaseOrderItemResponse;
import com.zhiyangyun.care.store.model.material.MaterialPurchaseOrderRequest;
import com.zhiyangyun.care.store.model.material.MaterialStockAmountItem;
import com.zhiyangyun.care.store.model.material.MaterialSupplierRequest;
import com.zhiyangyun.care.store.model.material.MaterialTransferItemRequest;
import com.zhiyangyun.care.store.model.material.MaterialTransferItemResponse;
import com.zhiyangyun.care.store.model.material.MaterialTransferOrderDetailResponse;
import com.zhiyangyun.care.store.model.material.MaterialTransferOrderRequest;
import com.zhiyangyun.care.store.model.material.MaterialWarehouseRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/material")
public class MaterialController {
  private final MaterialWarehouseMapper warehouseMapper;
  private final MaterialSupplierMapper supplierMapper;
  private final MaterialPurchaseOrderMapper purchaseOrderMapper;
  private final MaterialPurchaseOrderItemMapper purchaseOrderItemMapper;
  private final MaterialTransferOrderMapper transferOrderMapper;
  private final MaterialTransferItemMapper transferItemMapper;
  private final InventoryBatchMapper inventoryBatchMapper;
  private final ProductMapper productMapper;
  private final InventoryLogMapper inventoryLogMapper;
  private final AuditLogService auditLogService;

  public MaterialController(
      MaterialWarehouseMapper warehouseMapper,
      MaterialSupplierMapper supplierMapper,
      MaterialPurchaseOrderMapper purchaseOrderMapper,
      MaterialPurchaseOrderItemMapper purchaseOrderItemMapper,
      MaterialTransferOrderMapper transferOrderMapper,
      MaterialTransferItemMapper transferItemMapper,
      InventoryBatchMapper inventoryBatchMapper,
      ProductMapper productMapper,
      InventoryLogMapper inventoryLogMapper,
      AuditLogService auditLogService) {
    this.warehouseMapper = warehouseMapper;
    this.supplierMapper = supplierMapper;
    this.purchaseOrderMapper = purchaseOrderMapper;
    this.purchaseOrderItemMapper = purchaseOrderItemMapper;
    this.transferOrderMapper = transferOrderMapper;
    this.transferItemMapper = transferItemMapper;
    this.inventoryBatchMapper = inventoryBatchMapper;
    this.productMapper = productMapper;
    this.inventoryLogMapper = inventoryLogMapper;
    this.auditLogService = auditLogService;
  }

  @GetMapping("/warehouse/page")
  public Result<IPage<MaterialWarehouse>> warehousePage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    LambdaQueryWrapper<MaterialWarehouse> wrapper = Wrappers.lambdaQuery(MaterialWarehouse.class)
        .eq(MaterialWarehouse::getIsDeleted, 0)
        .eq(orgId != null, MaterialWarehouse::getOrgId, orgId)
        .orderByDesc(MaterialWarehouse::getCreateTime);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(MaterialWarehouse::getWarehouseName, keyword)
          .or().like(MaterialWarehouse::getWarehouseCode, keyword)
          .or().like(MaterialWarehouse::getManagerName, keyword));
    }
    return Result.ok(warehouseMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping("/warehouse")
  public Result<MaterialWarehouse> createWarehouse(@Valid @RequestBody MaterialWarehouseRequest request) {
    Long orgId = AuthContext.getOrgId();
    MaterialWarehouse row = new MaterialWarehouse();
    row.setOrgId(orgId);
    row.setWarehouseCode(request.getWarehouseCode());
    row.setWarehouseName(request.getWarehouseName());
    row.setManagerName(request.getManagerName());
    row.setManagerPhone(request.getManagerPhone());
    row.setAddress(request.getAddress());
    row.setStatus(request.getStatus() == null ? 1 : request.getStatus());
    row.setRemark(request.getRemark());
    warehouseMapper.insert(row);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "MATERIAL_WAREHOUSE", row.getId(), "新增仓库");
    return Result.ok(row);
  }

  @PutMapping("/warehouse/{id}")
  public Result<MaterialWarehouse> updateWarehouse(
      @PathVariable Long id,
      @Valid @RequestBody MaterialWarehouseRequest request) {
    Long orgId = AuthContext.getOrgId();
    MaterialWarehouse existing = warehouseMapper.selectById(id);
    if (existing == null || existing.getIsDeleted() != null && existing.getIsDeleted() == 1) {
      throw new IllegalArgumentException("warehouse not found");
    }
    ensureSameOrg(existing.getOrgId());
    existing.setWarehouseCode(request.getWarehouseCode());
    existing.setWarehouseName(request.getWarehouseName());
    existing.setManagerName(request.getManagerName());
    existing.setManagerPhone(request.getManagerPhone());
    existing.setAddress(request.getAddress());
    existing.setStatus(request.getStatus() == null ? 1 : request.getStatus());
    existing.setRemark(request.getRemark());
    warehouseMapper.updateById(existing);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "MATERIAL_WAREHOUSE", id, "更新仓库");
    return Result.ok(existing);
  }

  @DeleteMapping("/warehouse/{id}")
  public Result<Void> deleteWarehouse(@PathVariable Long id) {
    Long orgId = AuthContext.getOrgId();
    MaterialWarehouse existing = warehouseMapper.selectById(id);
    if (existing != null && (existing.getIsDeleted() == null || existing.getIsDeleted() == 0)) {
      ensureSameOrg(existing.getOrgId());
      existing.setIsDeleted(1);
      warehouseMapper.updateById(existing);
    }
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DELETE", "MATERIAL_WAREHOUSE", id, "删除仓库");
    return Result.ok(null);
  }

  @GetMapping("/supplier/page")
  public Result<IPage<MaterialSupplier>> supplierPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    LambdaQueryWrapper<MaterialSupplier> wrapper = Wrappers.lambdaQuery(MaterialSupplier.class)
        .eq(MaterialSupplier::getIsDeleted, 0)
        .eq(orgId != null, MaterialSupplier::getOrgId, orgId)
        .orderByDesc(MaterialSupplier::getCreateTime);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(MaterialSupplier::getSupplierName, keyword)
          .or().like(MaterialSupplier::getSupplierCode, keyword)
          .or().like(MaterialSupplier::getContactName, keyword));
    }
    return Result.ok(supplierMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping("/supplier")
  public Result<MaterialSupplier> createSupplier(@Valid @RequestBody MaterialSupplierRequest request) {
    Long orgId = AuthContext.getOrgId();
    MaterialSupplier row = new MaterialSupplier();
    row.setOrgId(orgId);
    row.setSupplierCode(request.getSupplierCode());
    row.setSupplierName(request.getSupplierName());
    row.setContactName(request.getContactName());
    row.setContactPhone(request.getContactPhone());
    row.setAddress(request.getAddress());
    row.setStatus(request.getStatus() == null ? 1 : request.getStatus());
    row.setRemark(request.getRemark());
    supplierMapper.insert(row);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "MATERIAL_SUPPLIER", row.getId(), "新增供应商");
    return Result.ok(row);
  }

  @PutMapping("/supplier/{id}")
  public Result<MaterialSupplier> updateSupplier(
      @PathVariable Long id,
      @Valid @RequestBody MaterialSupplierRequest request) {
    Long orgId = AuthContext.getOrgId();
    MaterialSupplier existing = supplierMapper.selectById(id);
    if (existing == null || existing.getIsDeleted() != null && existing.getIsDeleted() == 1) {
      throw new IllegalArgumentException("supplier not found");
    }
    ensureSameOrg(existing.getOrgId());
    existing.setSupplierCode(request.getSupplierCode());
    existing.setSupplierName(request.getSupplierName());
    existing.setContactName(request.getContactName());
    existing.setContactPhone(request.getContactPhone());
    existing.setAddress(request.getAddress());
    existing.setStatus(request.getStatus() == null ? 1 : request.getStatus());
    existing.setRemark(request.getRemark());
    supplierMapper.updateById(existing);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "MATERIAL_SUPPLIER", id, "更新供应商");
    return Result.ok(existing);
  }

  @DeleteMapping("/supplier/{id}")
  public Result<Void> deleteSupplier(@PathVariable Long id) {
    Long orgId = AuthContext.getOrgId();
    MaterialSupplier existing = supplierMapper.selectById(id);
    if (existing != null && (existing.getIsDeleted() == null || existing.getIsDeleted() == 0)) {
      ensureSameOrg(existing.getOrgId());
      existing.setIsDeleted(1);
      supplierMapper.updateById(existing);
    }
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DELETE", "MATERIAL_SUPPLIER", id, "删除供应商");
    return Result.ok(null);
  }

  @GetMapping("/purchase/page")
  public Result<IPage<MaterialPurchaseOrderDetailResponse>> purchasePage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long warehouseId,
      @RequestParam(required = false) Long supplierId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    LambdaQueryWrapper<MaterialPurchaseOrder> wrapper = Wrappers.lambdaQuery(MaterialPurchaseOrder.class)
        .eq(MaterialPurchaseOrder::getIsDeleted, 0)
        .eq(orgId != null, MaterialPurchaseOrder::getOrgId, orgId)
        .eq(warehouseId != null, MaterialPurchaseOrder::getWarehouseId, warehouseId)
        .eq(supplierId != null, MaterialPurchaseOrder::getSupplierId, supplierId)
        .eq(status != null && !status.isBlank(), MaterialPurchaseOrder::getStatus, status)
        .orderByDesc(MaterialPurchaseOrder::getCreateTime);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.like(MaterialPurchaseOrder::getOrderNo, keyword);
    }
    IPage<MaterialPurchaseOrder> page = purchaseOrderMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    IPage<MaterialPurchaseOrderDetailResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    List<Long> warehouseIds = page.getRecords().stream()
        .map(MaterialPurchaseOrder::getWarehouseId)
        .filter(java.util.Objects::nonNull)
        .distinct()
        .toList();
    List<Long> supplierIds = page.getRecords().stream()
        .map(MaterialPurchaseOrder::getSupplierId)
        .filter(java.util.Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, String> warehouseMap = warehouseIds.isEmpty()
        ? Map.of()
        : warehouseMapper.selectBatchIds(warehouseIds).stream()
            .collect(Collectors.toMap(MaterialWarehouse::getId, MaterialWarehouse::getWarehouseName));
    Map<Long, String> supplierMap = supplierIds.isEmpty()
        ? Map.of()
        : supplierMapper.selectBatchIds(supplierIds).stream()
            .collect(Collectors.toMap(MaterialSupplier::getId, MaterialSupplier::getSupplierName));
    resp.setRecords(page.getRecords().stream().map(order -> {
      MaterialPurchaseOrderDetailResponse item = new MaterialPurchaseOrderDetailResponse();
      item.setId(order.getId());
      item.setOrderNo(order.getOrderNo());
      item.setWarehouseId(order.getWarehouseId());
      item.setWarehouseName(warehouseMap.get(order.getWarehouseId()));
      item.setSupplierId(order.getSupplierId());
      item.setSupplierName(supplierMap.get(order.getSupplierId()));
      item.setOrderDate(order.getOrderDate());
      item.setStatus(order.getStatus());
      item.setTotalAmount(order.getTotalAmount());
      item.setRemark(order.getRemark());
      item.setCreateTime(order.getCreateTime());
      return item;
    }).toList());
    return Result.ok(resp);
  }

  @GetMapping("/purchase/{id}")
  public Result<MaterialPurchaseOrderDetailResponse> purchaseDetail(@PathVariable Long id) {
    MaterialPurchaseOrder order = purchaseOrderMapper.selectById(id);
    if (order == null || order.getIsDeleted() != null && order.getIsDeleted() == 1) {
      throw new IllegalArgumentException("purchase order not found");
    }
    ensureSameOrg(order.getOrgId());
    MaterialPurchaseOrderDetailResponse detail = buildPurchaseDetail(order);
    return Result.ok(detail);
  }

  @PostMapping("/purchase")
  @Transactional
  public Result<MaterialPurchaseOrderDetailResponse> createPurchase(
      @Valid @RequestBody MaterialPurchaseOrderRequest request) {
    Long orgId = AuthContext.getOrgId();
    MaterialPurchaseOrder order = new MaterialPurchaseOrder();
    order.setOrgId(orgId);
    order.setOrderNo(generateNo("PO"));
    order.setWarehouseId(request.getWarehouseId());
    order.setSupplierId(request.getSupplierId());
    order.setOrderDate(request.getOrderDate() == null ? LocalDate.now() : request.getOrderDate());
    order.setStatus("DRAFT");
    order.setRemark(request.getRemark());
    order.setTotalAmount(BigDecimal.ZERO);
    purchaseOrderMapper.insert(order);

    BigDecimal totalAmount = savePurchaseItems(orgId, order.getId(), request.getItems());
    order.setTotalAmount(totalAmount);
    purchaseOrderMapper.updateById(order);

    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "MATERIAL_PURCHASE_ORDER", order.getId(), "新增采购单");
    return Result.ok(buildPurchaseDetail(order));
  }

  @PutMapping("/purchase/{id}")
  @Transactional
  public Result<MaterialPurchaseOrderDetailResponse> updatePurchase(
      @PathVariable Long id,
      @Valid @RequestBody MaterialPurchaseOrderRequest request) {
    Long orgId = AuthContext.getOrgId();
    MaterialPurchaseOrder order = purchaseOrderMapper.selectById(id);
    if (order == null || order.getIsDeleted() != null && order.getIsDeleted() == 1) {
      throw new IllegalArgumentException("purchase order not found");
    }
    ensureSameOrg(order.getOrgId());
    if (!"DRAFT".equalsIgnoreCase(order.getStatus())) {
      throw new IllegalStateException("only draft purchase order can be updated");
    }
    order.setWarehouseId(request.getWarehouseId());
    order.setSupplierId(request.getSupplierId());
    order.setOrderDate(request.getOrderDate() == null ? LocalDate.now() : request.getOrderDate());
    order.setRemark(request.getRemark());

    purchaseOrderItemMapper.update(
        null,
        Wrappers.lambdaUpdate(MaterialPurchaseOrderItem.class)
            .set(MaterialPurchaseOrderItem::getIsDeleted, 1)
            .eq(MaterialPurchaseOrderItem::getOrderId, id)
            .eq(MaterialPurchaseOrderItem::getIsDeleted, 0));
    BigDecimal totalAmount = savePurchaseItems(orgId, id, request.getItems());
    order.setTotalAmount(totalAmount);
    purchaseOrderMapper.updateById(order);

    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "MATERIAL_PURCHASE_ORDER", id, "更新采购单");
    return Result.ok(buildPurchaseDetail(order));
  }

  @PostMapping("/purchase/{id}/approve")
  public Result<Void> approvePurchase(@PathVariable Long id) {
    Long orgId = AuthContext.getOrgId();
    MaterialPurchaseOrder order = purchaseOrderMapper.selectById(id);
    if (order == null || order.getIsDeleted() != null && order.getIsDeleted() == 1) {
      throw new IllegalArgumentException("purchase order not found");
    }
    ensureSameOrg(order.getOrgId());
    order.setStatus("APPROVED");
    purchaseOrderMapper.updateById(order);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "APPROVE", "MATERIAL_PURCHASE_ORDER", id, "审核采购单");
    return Result.ok(null);
  }

  @PostMapping("/purchase/{id}/complete")
  @Transactional
  public Result<Void> completePurchase(@PathVariable Long id) {
    Long orgId = AuthContext.getOrgId();
    MaterialPurchaseOrder order = purchaseOrderMapper.selectById(id);
    if (order == null || order.getIsDeleted() != null && order.getIsDeleted() == 1) {
      throw new IllegalArgumentException("purchase order not found");
    }
    ensureSameOrg(order.getOrgId());
    if (!"APPROVED".equalsIgnoreCase(order.getStatus())) {
      throw new IllegalStateException("only approved purchase order can be completed");
    }
    if (order.getWarehouseId() == null) {
      throw new IllegalStateException("purchase warehouse is required for inbound execution");
    }
    executePurchaseInbound(order);
    order.setStatus("COMPLETED");
    purchaseOrderMapper.updateById(order);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "COMPLETE", "MATERIAL_PURCHASE_ORDER", id, "采购单完成");
    return Result.ok(null);
  }

  @GetMapping("/transfer/page")
  public Result<IPage<MaterialTransferOrderDetailResponse>> transferPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    LambdaQueryWrapper<MaterialTransferOrder> wrapper = Wrappers.lambdaQuery(MaterialTransferOrder.class)
        .eq(MaterialTransferOrder::getIsDeleted, 0)
        .eq(orgId != null, MaterialTransferOrder::getOrgId, orgId)
        .eq(status != null && !status.isBlank(), MaterialTransferOrder::getStatus, status)
        .orderByDesc(MaterialTransferOrder::getCreateTime);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.like(MaterialTransferOrder::getTransferNo, keyword);
    }
    IPage<MaterialTransferOrder> page = transferOrderMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    IPage<MaterialTransferOrderDetailResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());

    List<Long> warehouseIds = new ArrayList<>();
    for (MaterialTransferOrder row : page.getRecords()) {
      if (row.getFromWarehouseId() != null) {
        warehouseIds.add(row.getFromWarehouseId());
      }
      if (row.getToWarehouseId() != null) {
        warehouseIds.add(row.getToWarehouseId());
      }
    }
    warehouseIds = warehouseIds.stream().distinct().toList();
    Map<Long, String> warehouseMap = warehouseIds.isEmpty()
        ? Map.of()
        : warehouseMapper.selectBatchIds(warehouseIds).stream()
            .collect(Collectors.toMap(MaterialWarehouse::getId, MaterialWarehouse::getWarehouseName));

    resp.setRecords(page.getRecords().stream().map(row -> {
      MaterialTransferOrderDetailResponse item = new MaterialTransferOrderDetailResponse();
      item.setId(row.getId());
      item.setTransferNo(row.getTransferNo());
      item.setFromWarehouseId(row.getFromWarehouseId());
      item.setFromWarehouseName(warehouseMap.get(row.getFromWarehouseId()));
      item.setToWarehouseId(row.getToWarehouseId());
      item.setToWarehouseName(warehouseMap.get(row.getToWarehouseId()));
      item.setStatus(row.getStatus());
      item.setRemark(row.getRemark());
      item.setCreateTime(row.getCreateTime());
      return item;
    }).toList());
    return Result.ok(resp);
  }

  @GetMapping("/transfer/{id}")
  public Result<MaterialTransferOrderDetailResponse> transferDetail(@PathVariable Long id) {
    MaterialTransferOrder order = transferOrderMapper.selectById(id);
    if (order == null || order.getIsDeleted() != null && order.getIsDeleted() == 1) {
      throw new IllegalArgumentException("transfer order not found");
    }
    ensureSameOrg(order.getOrgId());
    return Result.ok(buildTransferDetail(order));
  }

  @PostMapping("/transfer")
  @Transactional
  public Result<MaterialTransferOrderDetailResponse> createTransfer(
      @Valid @RequestBody MaterialTransferOrderRequest request) {
    Long orgId = AuthContext.getOrgId();
    if (request.getFromWarehouseId().equals(request.getToWarehouseId())) {
      throw new IllegalArgumentException("from warehouse and to warehouse cannot be same");
    }
    MaterialTransferOrder order = new MaterialTransferOrder();
    order.setOrgId(orgId);
    order.setTransferNo(generateNo("TR"));
    order.setFromWarehouseId(request.getFromWarehouseId());
    order.setToWarehouseId(request.getToWarehouseId());
    order.setStatus("DRAFT");
    order.setRemark(request.getRemark());
    transferOrderMapper.insert(order);
    saveTransferItems(orgId, order.getId(), request.getItems());
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "MATERIAL_TRANSFER_ORDER", order.getId(), "新增调拨单");
    return Result.ok(buildTransferDetail(order));
  }

  @PostMapping("/transfer/{id}/complete")
  @Transactional
  public Result<Void> completeTransfer(@PathVariable Long id) {
    Long orgId = AuthContext.getOrgId();
    MaterialTransferOrder order = transferOrderMapper.selectById(id);
    if (order == null || order.getIsDeleted() != null && order.getIsDeleted() == 1) {
      throw new IllegalArgumentException("transfer order not found");
    }
    ensureSameOrg(order.getOrgId());
    if (!"DRAFT".equalsIgnoreCase(order.getStatus())) {
      throw new IllegalStateException("only draft transfer order can be completed");
    }
    executeTransfer(order);
    order.setStatus("COMPLETED");
    transferOrderMapper.updateById(order);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "COMPLETE", "MATERIAL_TRANSFER_ORDER", id, "调拨单完成");
    return Result.ok(null);
  }

  @GetMapping("/stock/amount")
  public Result<List<MaterialStockAmountItem>> stockAmount(
      @RequestParam(defaultValue = "PRODUCT") String dimension,
      @RequestParam(required = false) Long warehouseId,
      @RequestParam(required = false) String category) {
    Long orgId = AuthContext.getOrgId();
    List<InventoryBatch> batches = inventoryBatchMapper.selectList(
        Wrappers.lambdaQuery(InventoryBatch.class)
            .eq(InventoryBatch::getIsDeleted, 0)
            .eq(warehouseId != null, InventoryBatch::getWarehouseId, warehouseId)
            .eq(orgId != null, InventoryBatch::getOrgId, orgId));
    List<Long> productIds = batches.stream()
        .map(InventoryBatch::getProductId)
        .filter(java.util.Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, Product> productMap = productIds.isEmpty()
        ? Map.of()
        : productMapper.selectBatchIds(productIds).stream()
            .collect(Collectors.toMap(Product::getId, p -> p));
    Map<Long, MaterialWarehouse> warehouseMap = batches.stream()
        .map(InventoryBatch::getWarehouseId)
        .filter(java.util.Objects::nonNull)
        .distinct()
        .collect(Collectors.toMap(
            id -> id,
            id -> warehouseMapper.selectById(id),
            (a, b) -> a));

    Map<String, MaterialStockAmountItem> resultMap = new HashMap<>();
    String dim = dimension == null ? "PRODUCT" : dimension.toUpperCase();
    for (InventoryBatch batch : batches) {
      Product product = productMap.get(batch.getProductId());
      if (category != null && !category.isBlank()) {
        String productCategory = product == null ? null : product.getCategory();
        if (productCategory == null || !productCategory.contains(category)) {
          continue;
        }
      }
      String key;
      if ("WAREHOUSE".equals(dim)) {
        key = "W-" + (batch.getWarehouseId() == null ? 0L : batch.getWarehouseId());
      } else if ("CATEGORY".equals(dim)) {
        key = "C-" + (product == null || product.getCategory() == null ? "UNSPECIFIED" : product.getCategory());
      } else {
        key = "P-" + batch.getProductId();
      }
      MaterialStockAmountItem item = resultMap.computeIfAbsent(key, k -> {
        MaterialStockAmountItem row = new MaterialStockAmountItem();
        row.setDimension(dim);
        row.setTotalQuantity(0);
        row.setTotalAmount(BigDecimal.ZERO);
        return row;
      });
      int qty = batch.getQuantity() == null ? 0 : batch.getQuantity();
      BigDecimal price = batch.getCostPrice() == null ? BigDecimal.ZERO : batch.getCostPrice();
      item.setTotalQuantity(item.getTotalQuantity() + qty);
      item.setTotalAmount(item.getTotalAmount().add(price.multiply(BigDecimal.valueOf(qty))));
      if ("WAREHOUSE".equals(dim)) {
        item.setWarehouseId(batch.getWarehouseId());
        MaterialWarehouse warehouse = batch.getWarehouseId() == null ? null : warehouseMap.get(batch.getWarehouseId());
        item.setWarehouseName(warehouse == null ? "未分配仓库" : warehouse.getWarehouseName());
      } else if ("CATEGORY".equals(dim)) {
        item.setCategory(
            product == null || product.getCategory() == null || product.getCategory().isBlank()
                ? "未分类"
                : product.getCategory());
      } else {
        if (product != null) {
          item.setProductId(product.getId());
          item.setProductCode(product.getProductCode());
          item.setProductName(product.getProductName());
          item.setCategory(product.getCategory());
        } else {
          item.setProductId(batch.getProductId());
        }
      }
    }

    List<MaterialStockAmountItem> list = new ArrayList<>(resultMap.values());
    list.sort((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()));
    return Result.ok(list);
  }

  private MaterialPurchaseOrderDetailResponse buildPurchaseDetail(MaterialPurchaseOrder order) {
    MaterialPurchaseOrderDetailResponse detail = new MaterialPurchaseOrderDetailResponse();
    detail.setId(order.getId());
    detail.setOrderNo(order.getOrderNo());
    detail.setWarehouseId(order.getWarehouseId());
    detail.setSupplierId(order.getSupplierId());
    detail.setOrderDate(order.getOrderDate());
    detail.setStatus(order.getStatus());
    detail.setTotalAmount(order.getTotalAmount());
    detail.setRemark(order.getRemark());
    detail.setCreateTime(order.getCreateTime());

    if (order.getWarehouseId() != null) {
      MaterialWarehouse warehouse = warehouseMapper.selectById(order.getWarehouseId());
      detail.setWarehouseName(warehouse == null ? null : warehouse.getWarehouseName());
    }
    if (order.getSupplierId() != null) {
      MaterialSupplier supplier = supplierMapper.selectById(order.getSupplierId());
      detail.setSupplierName(supplier == null ? null : supplier.getSupplierName());
    }

    List<MaterialPurchaseOrderItem> rows = purchaseOrderItemMapper.selectList(
        Wrappers.lambdaQuery(MaterialPurchaseOrderItem.class)
            .eq(MaterialPurchaseOrderItem::getOrderId, order.getId())
            .eq(MaterialPurchaseOrderItem::getIsDeleted, 0));
    List<MaterialPurchaseOrderItemResponse> itemResponses = rows.stream().map(row -> {
      MaterialPurchaseOrderItemResponse item = new MaterialPurchaseOrderItemResponse();
      item.setId(row.getId());
      item.setProductId(row.getProductId());
      item.setProductName(row.getProductNameSnapshot());
      item.setQuantity(row.getQuantity());
      item.setUnitPrice(row.getUnitPrice());
      item.setAmount(row.getAmount());
      return item;
    }).toList();
    detail.setItems(itemResponses);
    return detail;
  }

  private BigDecimal savePurchaseItems(Long orgId, Long orderId, List<MaterialPurchaseOrderItemRequest> items) {
    List<Long> productIds = items.stream()
        .map(MaterialPurchaseOrderItemRequest::getProductId)
        .distinct()
        .toList();
    Map<Long, Product> productMap = productIds.isEmpty()
        ? Map.of()
        : productMapper.selectBatchIds(productIds).stream()
            .collect(Collectors.toMap(Product::getId, p -> p));

    BigDecimal totalAmount = BigDecimal.ZERO;
    for (MaterialPurchaseOrderItemRequest req : items) {
      int qty = req.getQuantity() == null ? 0 : req.getQuantity();
      if (qty <= 0) {
        throw new IllegalArgumentException("purchase quantity must be positive");
      }
      BigDecimal unitPrice = req.getUnitPrice() == null ? BigDecimal.ZERO : req.getUnitPrice();
      BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(qty));
      totalAmount = totalAmount.add(amount);

      MaterialPurchaseOrderItem row = new MaterialPurchaseOrderItem();
      row.setOrgId(orgId);
      row.setOrderId(orderId);
      row.setProductId(req.getProductId());
      Product product = productMap.get(req.getProductId());
      row.setProductNameSnapshot(product == null ? null : product.getProductName());
      row.setQuantity(qty);
      row.setUnitPrice(unitPrice);
      row.setAmount(amount);
      purchaseOrderItemMapper.insert(row);
    }
    return totalAmount;
  }

  private MaterialTransferOrderDetailResponse buildTransferDetail(MaterialTransferOrder order) {
    MaterialTransferOrderDetailResponse detail = new MaterialTransferOrderDetailResponse();
    detail.setId(order.getId());
    detail.setTransferNo(order.getTransferNo());
    detail.setFromWarehouseId(order.getFromWarehouseId());
    detail.setToWarehouseId(order.getToWarehouseId());
    detail.setStatus(order.getStatus());
    detail.setRemark(order.getRemark());
    detail.setCreateTime(order.getCreateTime());

    MaterialWarehouse fromWarehouse = warehouseMapper.selectById(order.getFromWarehouseId());
    MaterialWarehouse toWarehouse = warehouseMapper.selectById(order.getToWarehouseId());
    detail.setFromWarehouseName(fromWarehouse == null ? null : fromWarehouse.getWarehouseName());
    detail.setToWarehouseName(toWarehouse == null ? null : toWarehouse.getWarehouseName());

    List<MaterialTransferItem> rows = transferItemMapper.selectList(
        Wrappers.lambdaQuery(MaterialTransferItem.class)
            .eq(MaterialTransferItem::getTransferId, order.getId())
            .eq(MaterialTransferItem::getIsDeleted, 0));
    List<MaterialTransferItemResponse> itemResponses = rows.stream().map(row -> {
      MaterialTransferItemResponse item = new MaterialTransferItemResponse();
      item.setId(row.getId());
      item.setProductId(row.getProductId());
      item.setProductName(row.getProductNameSnapshot());
      item.setQuantity(row.getQuantity());
      return item;
    }).toList();
    detail.setItems(itemResponses);
    return detail;
  }

  private void saveTransferItems(Long orgId, Long orderId, List<MaterialTransferItemRequest> items) {
    List<Long> productIds = items.stream()
        .map(MaterialTransferItemRequest::getProductId)
        .distinct()
        .toList();
    Map<Long, Product> productMap = productIds.isEmpty()
        ? Map.of()
        : productMapper.selectBatchIds(productIds).stream()
            .collect(Collectors.toMap(Product::getId, p -> p));

    for (MaterialTransferItemRequest req : items) {
      int qty = req.getQuantity() == null ? 0 : req.getQuantity();
      if (qty <= 0) {
        throw new IllegalArgumentException("transfer quantity must be positive");
      }
      MaterialTransferItem row = new MaterialTransferItem();
      row.setOrgId(orgId);
      row.setTransferId(orderId);
      row.setProductId(req.getProductId());
      Product product = productMap.get(req.getProductId());
      row.setProductNameSnapshot(product == null ? null : product.getProductName());
      row.setQuantity(qty);
      transferItemMapper.insert(row);
    }
  }

  private void executePurchaseInbound(MaterialPurchaseOrder order) {
    List<MaterialPurchaseOrderItem> items = purchaseOrderItemMapper.selectList(
        Wrappers.lambdaQuery(MaterialPurchaseOrderItem.class)
            .eq(MaterialPurchaseOrderItem::getOrderId, order.getId())
            .eq(MaterialPurchaseOrderItem::getIsDeleted, 0));
    if (items.isEmpty()) {
      throw new IllegalStateException("purchase order has no items");
    }
    String batchNo = "PO-" + order.getOrderNo();
    for (MaterialPurchaseOrderItem item : items) {
      InventoryBatch batch = inventoryBatchMapper.selectOne(
          Wrappers.lambdaQuery(InventoryBatch.class)
              .eq(InventoryBatch::getOrgId, order.getOrgId())
              .eq(InventoryBatch::getProductId, item.getProductId())
              .eq(InventoryBatch::getWarehouseId, order.getWarehouseId())
              .eq(InventoryBatch::getBatchNo, batchNo)
              .eq(InventoryBatch::getIsDeleted, 0));
      if (batch == null) {
        batch = new InventoryBatch();
        batch.setOrgId(order.getOrgId());
        batch.setProductId(item.getProductId());
        batch.setWarehouseId(order.getWarehouseId());
        batch.setBatchNo(batchNo);
        batch.setQuantity(item.getQuantity());
        batch.setCostPrice(item.getUnitPrice() == null ? BigDecimal.ZERO : item.getUnitPrice());
        batch.setWarehouseLocation(order.getWarehouseId() == null ? null : String.valueOf(order.getWarehouseId()));
        inventoryBatchMapper.insert(batch);
      } else {
        int current = batch.getQuantity() == null ? 0 : batch.getQuantity();
        batch.setQuantity(current + (item.getQuantity() == null ? 0 : item.getQuantity()));
        if (item.getUnitPrice() != null) {
          batch.setCostPrice(item.getUnitPrice());
        }
        inventoryBatchMapper.updateById(batch);
      }
      InventoryLog log = new InventoryLog();
      log.setOrgId(order.getOrgId());
      log.setProductId(item.getProductId());
      log.setBatchId(batch.getId());
      log.setWarehouseId(order.getWarehouseId());
      log.setChangeType("IN");
      log.setChangeQty(item.getQuantity());
      log.setBizType("PURCHASE");
      log.setRemark("采购单入库: " + order.getOrderNo());
      Product product = productMapper.selectById(item.getProductId());
      if (product != null) {
        log.setProductCodeSnapshot(product.getProductCode());
        log.setProductNameSnapshot(product.getProductName());
      }
      inventoryLogMapper.insert(log);
    }
  }

  private void executeTransfer(MaterialTransferOrder order) {
    List<MaterialTransferItem> items = transferItemMapper.selectList(
        Wrappers.lambdaQuery(MaterialTransferItem.class)
            .eq(MaterialTransferItem::getTransferId, order.getId())
            .eq(MaterialTransferItem::getIsDeleted, 0));
    if (items.isEmpty()) {
      throw new IllegalStateException("transfer order has no items");
    }
    for (MaterialTransferItem item : items) {
      transferProductAcrossWarehouse(order, item);
    }
  }

  private void transferProductAcrossWarehouse(MaterialTransferOrder order, MaterialTransferItem item) {
    int remaining = item.getQuantity() == null ? 0 : item.getQuantity();
    if (remaining <= 0) {
      throw new IllegalArgumentException("transfer quantity must be positive");
    }
    List<InventoryBatch> fromBatches = inventoryBatchMapper.selectList(
        Wrappers.lambdaQuery(InventoryBatch.class)
            .eq(InventoryBatch::getOrgId, order.getOrgId())
            .eq(InventoryBatch::getProductId, item.getProductId())
            .eq(InventoryBatch::getWarehouseId, order.getFromWarehouseId())
            .eq(InventoryBatch::getIsDeleted, 0)
            .orderByAsc(InventoryBatch::getExpireDate)
            .orderByAsc(InventoryBatch::getId));
    int total = fromBatches.stream().mapToInt(row -> row.getQuantity() == null ? 0 : row.getQuantity()).sum();
    if (total < remaining) {
      throw new IllegalStateException("insufficient stock for transfer");
    }
    Product product = productMapper.selectById(item.getProductId());
    for (InventoryBatch fromBatch : fromBatches) {
      if (remaining <= 0) {
        break;
      }
      int available = fromBatch.getQuantity() == null ? 0 : fromBatch.getQuantity();
      if (available <= 0) {
        continue;
      }
      int moved = Math.min(available, remaining);
      fromBatch.setQuantity(available - moved);
      inventoryBatchMapper.updateById(fromBatch);

      InventoryBatch toBatch = inventoryBatchMapper.selectOne(
          Wrappers.lambdaQuery(InventoryBatch.class)
              .eq(InventoryBatch::getOrgId, order.getOrgId())
              .eq(InventoryBatch::getProductId, item.getProductId())
              .eq(InventoryBatch::getWarehouseId, order.getToWarehouseId())
              .eq(InventoryBatch::getBatchNo, fromBatch.getBatchNo())
              .eq(InventoryBatch::getIsDeleted, 0));
      if (toBatch == null) {
        toBatch = new InventoryBatch();
        toBatch.setOrgId(order.getOrgId());
        toBatch.setProductId(item.getProductId());
        toBatch.setWarehouseId(order.getToWarehouseId());
        toBatch.setBatchNo(fromBatch.getBatchNo());
        toBatch.setQuantity(moved);
        toBatch.setCostPrice(fromBatch.getCostPrice());
        toBatch.setExpireDate(fromBatch.getExpireDate());
        toBatch.setWarehouseLocation(order.getToWarehouseId() == null ? null : String.valueOf(order.getToWarehouseId()));
        inventoryBatchMapper.insert(toBatch);
      } else {
        int current = toBatch.getQuantity() == null ? 0 : toBatch.getQuantity();
        toBatch.setQuantity(current + moved);
        inventoryBatchMapper.updateById(toBatch);
      }

      InventoryLog outLog = new InventoryLog();
      outLog.setOrgId(order.getOrgId());
      outLog.setProductId(item.getProductId());
      outLog.setBatchId(fromBatch.getId());
      outLog.setWarehouseId(order.getFromWarehouseId());
      outLog.setChangeType("OUT");
      outLog.setChangeQty(moved);
      outLog.setBizType("TRANSFER");
      outLog.setRemark("调拨出库: " + order.getTransferNo());
      if (product != null) {
        outLog.setProductCodeSnapshot(product.getProductCode());
        outLog.setProductNameSnapshot(product.getProductName());
      }
      inventoryLogMapper.insert(outLog);

      InventoryLog inLog = new InventoryLog();
      inLog.setOrgId(order.getOrgId());
      inLog.setProductId(item.getProductId());
      inLog.setBatchId(toBatch.getId());
      inLog.setWarehouseId(order.getToWarehouseId());
      inLog.setChangeType("IN");
      inLog.setChangeQty(moved);
      inLog.setBizType("TRANSFER");
      inLog.setRemark("调拨入库: " + order.getTransferNo());
      if (product != null) {
        inLog.setProductCodeSnapshot(product.getProductCode());
        inLog.setProductNameSnapshot(product.getProductName());
      }
      inventoryLogMapper.insert(inLog);
      remaining -= moved;
    }
  }

  private String generateNo(String prefix) {
    String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    int rand = new Random().nextInt(9000) + 1000;
    return prefix + date + rand;
  }

  private void ensureSameOrg(Long dataOrgId) {
    Long currentOrgId = AuthContext.getOrgId();
    if (currentOrgId != null && dataOrgId != null && !currentOrgId.equals(dataOrgId)) {
      throw new IllegalStateException("org mismatch");
    }
  }
}
