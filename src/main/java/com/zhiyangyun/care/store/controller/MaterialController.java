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
import com.zhiyangyun.care.store.model.material.MaterialEnableStatus;
import com.zhiyangyun.care.store.model.material.MaterialOrderStatus;
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
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
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
      @RequestParam(required = false) Boolean enabledOnly,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    LambdaQueryWrapper<MaterialWarehouse> wrapper = Wrappers.lambdaQuery(MaterialWarehouse.class)
        .eq(MaterialWarehouse::getIsDeleted, 0)
        .eq(orgId != null, MaterialWarehouse::getOrgId, orgId)
        .eq(Boolean.TRUE.equals(enabledOnly), MaterialWarehouse::getStatus, MaterialEnableStatus.ENABLED.code())
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
    String warehouseCode = normalizeText(request.getWarehouseCode());
    String warehouseName = normalizeText(request.getWarehouseName());
    validateStatusFlag(request.getStatus(), "warehouse status");
    ensureWarehouseCodeUnique(orgId, warehouseCode, null);
    MaterialWarehouse row = new MaterialWarehouse();
    row.setOrgId(orgId);
    row.setWarehouseCode(warehouseCode);
    row.setWarehouseName(warehouseName);
    row.setManagerName(normalizeNullableText(request.getManagerName()));
    row.setManagerPhone(normalizeNullableText(request.getManagerPhone()));
    row.setAddress(normalizeNullableText(request.getAddress()));
    row.setStatus(request.getStatus() == null ? MaterialEnableStatus.ENABLED.code() : request.getStatus());
    row.setRemark(normalizeNullableText(request.getRemark()));
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
    String warehouseCode = normalizeText(request.getWarehouseCode());
    String warehouseName = normalizeText(request.getWarehouseName());
    validateStatusFlag(request.getStatus(), "warehouse status");
    ensureWarehouseCodeUnique(orgId, warehouseCode, id);
    existing.setWarehouseCode(warehouseCode);
    existing.setWarehouseName(warehouseName);
    existing.setManagerName(normalizeNullableText(request.getManagerName()));
    existing.setManagerPhone(normalizeNullableText(request.getManagerPhone()));
    existing.setAddress(normalizeNullableText(request.getAddress()));
    existing.setStatus(request.getStatus() == null ? MaterialEnableStatus.ENABLED.code() : request.getStatus());
    existing.setRemark(normalizeNullableText(request.getRemark()));
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
      long purchaseRefCount = purchaseOrderMapper.selectCount(
          Wrappers.lambdaQuery(MaterialPurchaseOrder.class)
              .eq(MaterialPurchaseOrder::getWarehouseId, id)
              .eq(MaterialPurchaseOrder::getIsDeleted, 0)
              .eq(existing.getOrgId() != null, MaterialPurchaseOrder::getOrgId, existing.getOrgId()));
      long transferRefCount = transferOrderMapper.selectCount(
          Wrappers.lambdaQuery(MaterialTransferOrder.class)
              .eq(MaterialTransferOrder::getIsDeleted, 0)
              .eq(existing.getOrgId() != null, MaterialTransferOrder::getOrgId, existing.getOrgId())
              .and(w -> w.eq(MaterialTransferOrder::getFromWarehouseId, id)
                  .or().eq(MaterialTransferOrder::getToWarehouseId, id)));
      long batchRefCount = inventoryBatchMapper.selectCount(
          Wrappers.lambdaQuery(InventoryBatch.class)
              .eq(InventoryBatch::getWarehouseId, id)
              .eq(InventoryBatch::getIsDeleted, 0)
              .eq(existing.getOrgId() != null, InventoryBatch::getOrgId, existing.getOrgId()));
      if (purchaseRefCount > 0 || transferRefCount > 0 || batchRefCount > 0) {
        throw new IllegalStateException("warehouse is referenced and cannot be deleted");
      }
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
      @RequestParam(required = false) Boolean enabledOnly,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    LambdaQueryWrapper<MaterialSupplier> wrapper = Wrappers.lambdaQuery(MaterialSupplier.class)
        .eq(MaterialSupplier::getIsDeleted, 0)
        .eq(orgId != null, MaterialSupplier::getOrgId, orgId)
        .eq(Boolean.TRUE.equals(enabledOnly), MaterialSupplier::getStatus, MaterialEnableStatus.ENABLED.code())
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
    String supplierCode = normalizeText(request.getSupplierCode());
    String supplierName = normalizeText(request.getSupplierName());
    validateStatusFlag(request.getStatus(), "supplier status");
    ensureSupplierCodeUnique(orgId, supplierCode, null);
    MaterialSupplier row = new MaterialSupplier();
    row.setOrgId(orgId);
    row.setSupplierCode(supplierCode);
    row.setSupplierName(supplierName);
    row.setContactName(normalizeNullableText(request.getContactName()));
    row.setContactPhone(normalizeNullableText(request.getContactPhone()));
    row.setAddress(normalizeNullableText(request.getAddress()));
    row.setStatus(request.getStatus() == null ? MaterialEnableStatus.ENABLED.code() : request.getStatus());
    row.setRemark(normalizeNullableText(request.getRemark()));
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
    String supplierCode = normalizeText(request.getSupplierCode());
    String supplierName = normalizeText(request.getSupplierName());
    validateStatusFlag(request.getStatus(), "supplier status");
    ensureSupplierCodeUnique(orgId, supplierCode, id);
    existing.setSupplierCode(supplierCode);
    existing.setSupplierName(supplierName);
    existing.setContactName(normalizeNullableText(request.getContactName()));
    existing.setContactPhone(normalizeNullableText(request.getContactPhone()));
    existing.setAddress(normalizeNullableText(request.getAddress()));
    existing.setStatus(request.getStatus() == null ? MaterialEnableStatus.ENABLED.code() : request.getStatus());
    existing.setRemark(normalizeNullableText(request.getRemark()));
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
      long purchaseRefCount = purchaseOrderMapper.selectCount(
          Wrappers.lambdaQuery(MaterialPurchaseOrder.class)
              .eq(MaterialPurchaseOrder::getSupplierId, id)
              .eq(MaterialPurchaseOrder::getIsDeleted, 0)
              .eq(existing.getOrgId() != null, MaterialPurchaseOrder::getOrgId, existing.getOrgId()));
      if (purchaseRefCount > 0) {
        throw new IllegalStateException("supplier is referenced and cannot be deleted");
      }
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
    MaterialOrderStatus queryStatus = MaterialOrderStatus.from(status);
    String queryStatusCode = queryStatus == null ? null : queryStatus.code();
    LambdaQueryWrapper<MaterialPurchaseOrder> wrapper = Wrappers.lambdaQuery(MaterialPurchaseOrder.class)
        .eq(MaterialPurchaseOrder::getIsDeleted, 0)
        .eq(orgId != null, MaterialPurchaseOrder::getOrgId, orgId)
        .eq(warehouseId != null, MaterialPurchaseOrder::getWarehouseId, warehouseId)
        .eq(supplierId != null, MaterialPurchaseOrder::getSupplierId, supplierId)
        .eq(queryStatusCode != null, MaterialPurchaseOrder::getStatus, queryStatusCode)
        .orderByDesc(MaterialPurchaseOrder::getCreateTime);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.like(MaterialPurchaseOrder::getOrderNo, keyword);
    }
    IPage<MaterialPurchaseOrder> page = purchaseOrderMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    IPage<MaterialPurchaseOrderDetailResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    List<Long> warehouseIds = page.getRecords().stream()
        .map(MaterialPurchaseOrder::getWarehouseId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    List<Long> supplierIds = page.getRecords().stream()
        .map(MaterialPurchaseOrder::getSupplierId)
        .filter(Objects::nonNull)
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
    MaterialWarehouse warehouse = requireWarehouse(orgId, request.getWarehouseId(), true);
    MaterialSupplier supplier = requireSupplier(orgId, request.getSupplierId(), true);
    MaterialPurchaseOrder order = new MaterialPurchaseOrder();
    order.setOrgId(orgId);
    order.setOrderNo(generateNo("PO"));
    order.setWarehouseId(warehouse.getId());
    order.setSupplierId(supplier.getId());
    order.setOrderDate(request.getOrderDate() == null ? LocalDate.now() : request.getOrderDate());
    order.setStatus(MaterialOrderStatus.DRAFT.code());
    order.setRemark(normalizeNullableText(request.getRemark()));
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
    if (!MaterialOrderStatus.DRAFT.code().equalsIgnoreCase(order.getStatus())) {
      throw new IllegalStateException("only draft purchase order can be updated");
    }
    MaterialWarehouse warehouse = requireWarehouse(orgId, request.getWarehouseId(), true);
    MaterialSupplier supplier = requireSupplier(orgId, request.getSupplierId(), true);
    order.setWarehouseId(warehouse.getId());
    order.setSupplierId(supplier.getId());
    order.setOrderDate(request.getOrderDate() == null ? LocalDate.now() : request.getOrderDate());
    order.setRemark(normalizeNullableText(request.getRemark()));

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
    if (!MaterialOrderStatus.DRAFT.code().equalsIgnoreCase(order.getStatus())) {
      throw new IllegalStateException("only draft purchase order can be approved");
    }
    order.setStatus(MaterialOrderStatus.APPROVED.code());
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
    if (!MaterialOrderStatus.APPROVED.code().equalsIgnoreCase(order.getStatus())) {
      throw new IllegalStateException("only approved purchase order can be completed");
    }
    if (order.getWarehouseId() == null) {
      throw new IllegalStateException("purchase warehouse is required for inbound execution");
    }
    requireWarehouse(order.getOrgId(), order.getWarehouseId(), false);
    executePurchaseInbound(order);
    order.setStatus(MaterialOrderStatus.COMPLETED.code());
    purchaseOrderMapper.updateById(order);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "COMPLETE", "MATERIAL_PURCHASE_ORDER", id, "采购单完成");
    return Result.ok(null);
  }

  @PostMapping("/purchase/{id}/cancel")
  public Result<Void> cancelPurchase(@PathVariable Long id) {
    Long orgId = AuthContext.getOrgId();
    MaterialPurchaseOrder order = purchaseOrderMapper.selectById(id);
    if (order == null || order.getIsDeleted() != null && order.getIsDeleted() == 1) {
      throw new IllegalArgumentException("purchase order not found");
    }
    ensureSameOrg(order.getOrgId());
    if (MaterialOrderStatus.COMPLETED.code().equalsIgnoreCase(order.getStatus())) {
      throw new IllegalStateException("completed purchase order cannot be cancelled");
    }
    if (MaterialOrderStatus.CANCELLED.code().equalsIgnoreCase(order.getStatus())) {
      throw new IllegalStateException("purchase order is already cancelled");
    }
    order.setStatus(MaterialOrderStatus.CANCELLED.code());
    purchaseOrderMapper.updateById(order);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CANCEL", "MATERIAL_PURCHASE_ORDER", id, "采购单作废");
    return Result.ok(null);
  }

  @GetMapping("/transfer/page")
  public Result<IPage<MaterialTransferOrderDetailResponse>> transferPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    MaterialOrderStatus queryStatus = MaterialOrderStatus.from(status);
    String queryStatusCode = queryStatus == null ? null : queryStatus.code();
    if (queryStatus == MaterialOrderStatus.APPROVED) {
      throw new IllegalArgumentException("transfer status does not support APPROVED");
    }
    LambdaQueryWrapper<MaterialTransferOrder> wrapper = Wrappers.lambdaQuery(MaterialTransferOrder.class)
        .eq(MaterialTransferOrder::getIsDeleted, 0)
        .eq(orgId != null, MaterialTransferOrder::getOrgId, orgId)
        .eq(queryStatusCode != null, MaterialTransferOrder::getStatus, queryStatusCode)
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
    MaterialWarehouse fromWarehouse = requireWarehouse(orgId, request.getFromWarehouseId(), true);
    MaterialWarehouse toWarehouse = requireWarehouse(orgId, request.getToWarehouseId(), true);
    MaterialTransferOrder order = new MaterialTransferOrder();
    order.setOrgId(orgId);
    order.setTransferNo(generateNo("TR"));
    order.setFromWarehouseId(fromWarehouse.getId());
    order.setToWarehouseId(toWarehouse.getId());
    order.setStatus(MaterialOrderStatus.DRAFT.code());
    order.setRemark(normalizeNullableText(request.getRemark()));
    transferOrderMapper.insert(order);
    saveTransferItems(orgId, order.getId(), request.getItems());
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "MATERIAL_TRANSFER_ORDER", order.getId(), "新增调拨单");
    return Result.ok(buildTransferDetail(order));
  }

  @PutMapping("/transfer/{id}")
  @Transactional
  public Result<MaterialTransferOrderDetailResponse> updateTransfer(
      @PathVariable Long id,
      @Valid @RequestBody MaterialTransferOrderRequest request) {
    Long orgId = AuthContext.getOrgId();
    MaterialTransferOrder order = transferOrderMapper.selectById(id);
    if (order == null || order.getIsDeleted() != null && order.getIsDeleted() == 1) {
      throw new IllegalArgumentException("transfer order not found");
    }
    ensureSameOrg(order.getOrgId());
    if (!MaterialOrderStatus.DRAFT.code().equalsIgnoreCase(order.getStatus())) {
      throw new IllegalStateException("only draft transfer order can be updated");
    }
    if (request.getFromWarehouseId().equals(request.getToWarehouseId())) {
      throw new IllegalArgumentException("from warehouse and to warehouse cannot be same");
    }
    MaterialWarehouse fromWarehouse = requireWarehouse(orgId, request.getFromWarehouseId(), true);
    MaterialWarehouse toWarehouse = requireWarehouse(orgId, request.getToWarehouseId(), true);
    order.setFromWarehouseId(fromWarehouse.getId());
    order.setToWarehouseId(toWarehouse.getId());
    order.setRemark(normalizeNullableText(request.getRemark()));
    transferOrderMapper.updateById(order);

    transferItemMapper.update(
        null,
        Wrappers.lambdaUpdate(MaterialTransferItem.class)
            .set(MaterialTransferItem::getIsDeleted, 1)
            .eq(MaterialTransferItem::getTransferId, id)
            .eq(MaterialTransferItem::getIsDeleted, 0));
    saveTransferItems(orgId, id, request.getItems());

    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "MATERIAL_TRANSFER_ORDER", id, "更新调拨单");
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
    if (!MaterialOrderStatus.DRAFT.code().equalsIgnoreCase(order.getStatus())) {
      throw new IllegalStateException("only draft transfer order can be completed");
    }
    requireWarehouse(order.getOrgId(), order.getFromWarehouseId(), false);
    requireWarehouse(order.getOrgId(), order.getToWarehouseId(), false);
    executeTransfer(order);
    order.setStatus(MaterialOrderStatus.COMPLETED.code());
    transferOrderMapper.updateById(order);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "COMPLETE", "MATERIAL_TRANSFER_ORDER", id, "调拨单完成");
    return Result.ok(null);
  }

  @PostMapping("/transfer/{id}/cancel")
  public Result<Void> cancelTransfer(@PathVariable Long id) {
    Long orgId = AuthContext.getOrgId();
    MaterialTransferOrder order = transferOrderMapper.selectById(id);
    if (order == null || order.getIsDeleted() != null && order.getIsDeleted() == 1) {
      throw new IllegalArgumentException("transfer order not found");
    }
    ensureSameOrg(order.getOrgId());
    if (MaterialOrderStatus.COMPLETED.code().equalsIgnoreCase(order.getStatus())) {
      throw new IllegalStateException("completed transfer order cannot be cancelled");
    }
    if (MaterialOrderStatus.CANCELLED.code().equalsIgnoreCase(order.getStatus())) {
      throw new IllegalStateException("transfer order is already cancelled");
    }
    order.setStatus(MaterialOrderStatus.CANCELLED.code());
    transferOrderMapper.updateById(order);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CANCEL", "MATERIAL_TRANSFER_ORDER", id, "调拨单作废");
    return Result.ok(null);
  }

  @GetMapping("/stock/amount")
  public Result<List<MaterialStockAmountItem>> stockAmount(
      @RequestParam(defaultValue = "PRODUCT") String dimension,
      @RequestParam(required = false) Long warehouseId,
      @RequestParam(required = false) String category) {
    Long orgId = AuthContext.getOrgId();
    String dim = normalizeStockDimension(dimension);
    List<InventoryBatch> batches = inventoryBatchMapper.selectList(
        Wrappers.lambdaQuery(InventoryBatch.class)
            .eq(InventoryBatch::getIsDeleted, 0)
            .eq(warehouseId != null, InventoryBatch::getWarehouseId, warehouseId)
            .eq(orgId != null, InventoryBatch::getOrgId, orgId));
    List<Long> productIds = batches.stream()
        .map(InventoryBatch::getProductId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, Product> productMap = productIds.isEmpty()
        ? Map.of()
        : productMapper.selectBatchIds(productIds).stream()
            .collect(Collectors.toMap(Product::getId, p -> p));
    Map<Long, MaterialWarehouse> warehouseMap = batches.stream()
        .map(InventoryBatch::getWarehouseId)
        .filter(Objects::nonNull)
        .distinct()
        .collect(Collectors.collectingAndThen(
            Collectors.toList(),
            ids -> ids.isEmpty()
                ? Map.<Long, MaterialWarehouse>of()
                : warehouseMapper.selectBatchIds(ids).stream()
                    .filter(row -> row.getIsDeleted() == null || row.getIsDeleted() == 0)
                    .collect(Collectors.toMap(MaterialWarehouse::getId, row -> row))));

    Map<String, MaterialStockAmountItem> resultMap = new HashMap<>();
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
    ensureNoDuplicateProduct(items.stream().map(MaterialPurchaseOrderItemRequest::getProductId).toList(), "purchase");
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
      if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
        throw new IllegalArgumentException("unit price must not be negative");
      }
      BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(qty));
      totalAmount = totalAmount.add(amount);

      MaterialPurchaseOrderItem row = new MaterialPurchaseOrderItem();
      row.setOrgId(orgId);
      row.setOrderId(orderId);
      row.setProductId(req.getProductId());
      Product product = requireProduct(orgId, productMap.get(req.getProductId()));
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
    ensureNoDuplicateProduct(items.stream().map(MaterialTransferItemRequest::getProductId).toList(), "transfer");
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
      Product product = requireProduct(orgId, productMap.get(req.getProductId()));
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

  private void ensureWarehouseCodeUnique(Long orgId, String warehouseCode, Long excludeId) {
    LambdaQueryWrapper<MaterialWarehouse> wrapper = Wrappers.lambdaQuery(MaterialWarehouse.class)
        .eq(MaterialWarehouse::getWarehouseCode, warehouseCode)
        .eq(MaterialWarehouse::getIsDeleted, 0)
        .eq(orgId != null, MaterialWarehouse::getOrgId, orgId)
        .ne(excludeId != null, MaterialWarehouse::getId, excludeId);
    long count = warehouseMapper.selectCount(wrapper);
    if (count > 0) {
      throw new IllegalArgumentException("warehouse code already exists");
    }
  }

  private void ensureSupplierCodeUnique(Long orgId, String supplierCode, Long excludeId) {
    LambdaQueryWrapper<MaterialSupplier> wrapper = Wrappers.lambdaQuery(MaterialSupplier.class)
        .eq(MaterialSupplier::getSupplierCode, supplierCode)
        .eq(MaterialSupplier::getIsDeleted, 0)
        .eq(orgId != null, MaterialSupplier::getOrgId, orgId)
        .ne(excludeId != null, MaterialSupplier::getId, excludeId);
    long count = supplierMapper.selectCount(wrapper);
    if (count > 0) {
      throw new IllegalArgumentException("supplier code already exists");
    }
  }

  private MaterialWarehouse requireWarehouse(Long orgId, Long warehouseId, boolean mustEnabled) {
    if (warehouseId == null) {
      throw new IllegalArgumentException("warehouse is required");
    }
    MaterialWarehouse warehouse = warehouseMapper.selectById(warehouseId);
    if (warehouse == null || warehouse.getIsDeleted() != null && warehouse.getIsDeleted() == 1) {
      throw new IllegalArgumentException("warehouse not found");
    }
    if (orgId != null && warehouse.getOrgId() != null && !orgId.equals(warehouse.getOrgId())) {
      throw new IllegalStateException("warehouse org mismatch");
    }
    if (mustEnabled && Integer.valueOf(MaterialEnableStatus.DISABLED.code()).equals(warehouse.getStatus())) {
      throw new IllegalStateException("warehouse is disabled");
    }
    return warehouse;
  }

  private MaterialSupplier requireSupplier(Long orgId, Long supplierId, boolean mustEnabled) {
    if (supplierId == null) {
      throw new IllegalArgumentException("supplier is required");
    }
    MaterialSupplier supplier = supplierMapper.selectById(supplierId);
    if (supplier == null || supplier.getIsDeleted() != null && supplier.getIsDeleted() == 1) {
      throw new IllegalArgumentException("supplier not found");
    }
    if (orgId != null && supplier.getOrgId() != null && !orgId.equals(supplier.getOrgId())) {
      throw new IllegalStateException("supplier org mismatch");
    }
    if (mustEnabled && Integer.valueOf(MaterialEnableStatus.DISABLED.code()).equals(supplier.getStatus())) {
      throw new IllegalStateException("supplier is disabled");
    }
    return supplier;
  }

  private Product requireProduct(Long orgId, Product product) {
    if (product == null || product.getIsDeleted() != null && product.getIsDeleted() == 1) {
      throw new IllegalArgumentException("product not found");
    }
    if (orgId != null && product.getOrgId() != null && !orgId.equals(product.getOrgId())) {
      throw new IllegalStateException("product org mismatch");
    }
    if (Integer.valueOf(MaterialEnableStatus.DISABLED.code()).equals(product.getStatus())) {
      throw new IllegalStateException("product is disabled");
    }
    return product;
  }

  private void ensureNoDuplicateProduct(List<Long> productIds, String bizName) {
    Set<Long> unique = new HashSet<>();
    for (Long productId : productIds) {
      if (!unique.add(productId)) {
        throw new IllegalArgumentException(bizName + " items contain duplicate product");
      }
    }
  }

  private void validateStatusFlag(Integer status, String fieldName) {
    if (status == null) {
      return;
    }
    MaterialEnableStatus.from(status);
  }

  private String normalizeText(String value) {
    if (value == null) {
      return null;
    }
    return value.trim();
  }

  private String normalizeNullableText(String value) {
    if (value == null) {
      return null;
    }
    String normalized = value.trim();
    return normalized.isEmpty() ? null : normalized;
  }

  private String generateNo(String prefix) {
    String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    int rand = ThreadLocalRandom.current().nextInt(1000, 10000);
    return prefix + date + rand;
  }

  private String normalizeStockDimension(String dimension) {
    String dim = dimension == null ? "PRODUCT" : dimension.trim().toUpperCase();
    if (!"PRODUCT".equals(dim) && !"WAREHOUSE".equals(dim) && !"CATEGORY".equals(dim)) {
      throw new IllegalArgumentException("dimension must be PRODUCT, WAREHOUSE or CATEGORY");
    }
    return dim;
  }

  private void ensureSameOrg(Long dataOrgId) {
    Long currentOrgId = AuthContext.getOrgId();
    if (currentOrgId != null && dataOrgId != null && !currentOrgId.equals(dataOrgId)) {
      throw new IllegalStateException("org mismatch");
    }
  }
}
