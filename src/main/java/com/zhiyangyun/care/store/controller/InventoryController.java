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
import com.zhiyangyun.care.store.entity.InventoryOutboundSheet;
import com.zhiyangyun.care.store.entity.InventoryOutboundSheetItem;
import com.zhiyangyun.care.store.entity.MaterialWarehouse;
import com.zhiyangyun.care.store.entity.Product;
import com.zhiyangyun.care.store.mapper.InventoryBatchMapper;
import com.zhiyangyun.care.store.mapper.InventoryAdjustmentMapper;
import com.zhiyangyun.care.store.mapper.InventoryLogMapper;
import com.zhiyangyun.care.store.mapper.InventoryOutboundSheetItemMapper;
import com.zhiyangyun.care.store.mapper.InventoryOutboundSheetMapper;
import com.zhiyangyun.care.store.mapper.MaterialWarehouseMapper;
import com.zhiyangyun.care.store.mapper.ProductMapper;
import com.zhiyangyun.care.store.model.InventoryAdjustRequest;
import com.zhiyangyun.care.store.model.InventoryAlertItem;
import com.zhiyangyun.care.store.model.InventoryBatchResponse;
import com.zhiyangyun.care.store.model.InventoryExpiryAlertItem;
import com.zhiyangyun.care.store.model.InventoryInboundRequest;
import com.zhiyangyun.care.store.model.InventoryOutboundRequest;
import com.zhiyangyun.care.store.model.InventoryOutboundSheetCreateRequest;
import com.zhiyangyun.care.store.model.InventoryOutboundSheetItemRequest;
import com.zhiyangyun.care.store.model.InventoryOutboundSheetItemResponse;
import com.zhiyangyun.care.store.model.InventoryOutboundSheetResponse;
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
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
  private final InventoryService inventoryService;
  private final InventoryBatchMapper inventoryBatchMapper;
  private final InventoryAdjustmentMapper inventoryAdjustmentMapper;
  private final InventoryLogMapper inventoryLogMapper;
  private final InventoryOutboundSheetMapper inventoryOutboundSheetMapper;
  private final InventoryOutboundSheetItemMapper inventoryOutboundSheetItemMapper;
  private final ProductMapper productMapper;
  private final MaterialWarehouseMapper materialWarehouseMapper;
  private final AuditLogService auditLogService;

  public InventoryController(InventoryService inventoryService,
      InventoryBatchMapper inventoryBatchMapper,
      InventoryAdjustmentMapper inventoryAdjustmentMapper,
      InventoryLogMapper inventoryLogMapper,
      InventoryOutboundSheetMapper inventoryOutboundSheetMapper,
      InventoryOutboundSheetItemMapper inventoryOutboundSheetItemMapper,
      ProductMapper productMapper,
      MaterialWarehouseMapper materialWarehouseMapper,
      AuditLogService auditLogService) {
    this.inventoryService = inventoryService;
    this.inventoryBatchMapper = inventoryBatchMapper;
    this.inventoryAdjustmentMapper = inventoryAdjustmentMapper;
    this.inventoryLogMapper = inventoryLogMapper;
    this.inventoryOutboundSheetMapper = inventoryOutboundSheetMapper;
    this.inventoryOutboundSheetItemMapper = inventoryOutboundSheetItemMapper;
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

  @PostMapping("/outbound/sheet")
  @Transactional
  public Result<InventoryOutboundSheetResponse> createOutboundSheet(@Valid @RequestBody InventoryOutboundSheetCreateRequest request) {
    Long orgId = AuthContext.getOrgId();
    InventoryOutboundSheet sheet = new InventoryOutboundSheet();
    sheet.setOrgId(orgId);
    sheet.setOutboundNo(resolveOutboundNo(orgId, request.getOutboundNo()));
    sheet.setReceiverName(request.getReceiverName());
    sheet.setElderId(request.getElderId());
    sheet.setContractNo(request.getContractNo());
    sheet.setApplyDept(request.getApplyDept());
    sheet.setOperatorStaffId(AuthContext.getStaffId());
    sheet.setOperatorName(AuthContext.getUsername());
    sheet.setRemark(request.getRemark());
    sheet.setStatus("DRAFT");
    sheet.setIsDeleted(0);
    inventoryOutboundSheetMapper.insert(sheet);

    List<InventoryOutboundSheetItemRequest> itemRequests = request.getItems();
    int sort = 1;
    for (InventoryOutboundSheetItemRequest item : itemRequests) {
      InventoryOutboundSheetItem row = new InventoryOutboundSheetItem();
      row.setOrgId(orgId);
      row.setSheetId(sheet.getId());
      row.setProductId(item.getProductId());
      row.setWarehouseId(item.getWarehouseId());
      row.setBatchId(item.getBatchId());
      row.setQuantity(item.getQuantity());
      row.setReason(item.getReason());
      row.setItemSort(sort++);
      row.setIsDeleted(0);
      inventoryOutboundSheetItemMapper.insert(row);
    }
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "INVENTORY_OUTBOUND_SHEET_CREATE", "OUTBOUND_SHEET", sheet.getId(), "创建领用单:" + sheet.getOutboundNo());
    return Result.ok(getOutboundSheetDetail(sheet.getId()).getData());
  }

  @GetMapping("/outbound/sheet/page")
  public Result<IPage<InventoryOutboundSheetResponse>> outboundSheetPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo) {
    Long orgId = AuthContext.getOrgId();
    LambdaQueryWrapper<InventoryOutboundSheet> wrapper = Wrappers.lambdaQuery(InventoryOutboundSheet.class)
        .eq(InventoryOutboundSheet::getIsDeleted, 0)
        .eq(orgId != null, InventoryOutboundSheet::getOrgId, orgId)
        .eq(status != null && !status.isBlank(), InventoryOutboundSheet::getStatus, status)
        .and(keyword != null && !keyword.isBlank(), w -> w.like(InventoryOutboundSheet::getOutboundNo, keyword)
            .or().like(InventoryOutboundSheet::getReceiverName, keyword));
    if (dateFrom != null && !dateFrom.isBlank()) {
      LocalDate start = LocalDate.parse(dateFrom);
      wrapper.ge(InventoryOutboundSheet::getCreateTime, LocalDateTime.of(start, LocalTime.MIN));
    }
    if (dateTo != null && !dateTo.isBlank()) {
      LocalDate end = LocalDate.parse(dateTo);
      wrapper.le(InventoryOutboundSheet::getCreateTime, LocalDateTime.of(end, LocalTime.MAX));
    }
    wrapper.orderByDesc(InventoryOutboundSheet::getCreateTime);
    IPage<InventoryOutboundSheet> page = inventoryOutboundSheetMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    List<Long> sheetIds = page.getRecords().stream().map(InventoryOutboundSheet::getId).toList();
    Map<Long, List<InventoryOutboundSheetItem>> itemMap = sheetIds.isEmpty() ? Map.of() :
        inventoryOutboundSheetItemMapper.selectList(
                Wrappers.lambdaQuery(InventoryOutboundSheetItem.class)
                    .eq(InventoryOutboundSheetItem::getIsDeleted, 0)
                    .eq(orgId != null, InventoryOutboundSheetItem::getOrgId, orgId)
                    .in(InventoryOutboundSheetItem::getSheetId, sheetIds)
                    .orderByAsc(InventoryOutboundSheetItem::getItemSort)
                    .orderByAsc(InventoryOutboundSheetItem::getId))
            .stream().collect(Collectors.groupingBy(InventoryOutboundSheetItem::getSheetId));
    Map<Long, Product> productMap = itemMap.values().stream()
        .flatMap(List::stream)
        .map(InventoryOutboundSheetItem::getProductId)
        .filter(Objects::nonNull)
        .distinct()
        .collect(Collectors.collectingAndThen(Collectors.toList(), ids ->
            ids.isEmpty() ? Map.of() : productMapper.selectBatchIds(ids).stream().collect(Collectors.toMap(Product::getId, p -> p))));
    Map<Long, InventoryBatch> batchMap = itemMap.values().stream()
        .flatMap(List::stream)
        .map(InventoryOutboundSheetItem::getBatchId)
        .filter(Objects::nonNull)
        .distinct()
        .collect(Collectors.collectingAndThen(Collectors.toList(), ids ->
            ids.isEmpty() ? Map.of() : inventoryBatchMapper.selectBatchIds(ids).stream().collect(Collectors.toMap(InventoryBatch::getId, b -> b))));
    IPage<InventoryOutboundSheetResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream()
        .map(sheet -> toOutboundSheetResponse(sheet, itemMap.get(sheet.getId()), productMap, batchMap))
        .toList());
    return Result.ok(resp);
  }

  @GetMapping("/outbound/sheet/detail")
  public Result<InventoryOutboundSheetResponse> getOutboundSheetDetail(@RequestParam Long id) {
    Long orgId = AuthContext.getOrgId();
    InventoryOutboundSheet sheet = inventoryOutboundSheetMapper.selectById(id);
    if (sheet == null || sheet.getIsDeleted() == 1) {
      return Result.ok(null);
    }
    if (orgId != null && !orgId.equals(sheet.getOrgId())) {
      throw new IllegalArgumentException("无权限访问该领用单");
    }
    List<InventoryOutboundSheetItem> itemList = inventoryOutboundSheetItemMapper.selectList(
        Wrappers.lambdaQuery(InventoryOutboundSheetItem.class)
            .eq(InventoryOutboundSheetItem::getIsDeleted, 0)
            .eq(InventoryOutboundSheetItem::getSheetId, sheet.getId())
            .orderByAsc(InventoryOutboundSheetItem::getItemSort)
            .orderByAsc(InventoryOutboundSheetItem::getId));
    Map<Long, Product> productMap = itemList.stream()
        .map(InventoryOutboundSheetItem::getProductId)
        .filter(Objects::nonNull)
        .distinct()
        .collect(Collectors.collectingAndThen(Collectors.toList(), ids ->
            ids.isEmpty() ? Map.of() : productMapper.selectBatchIds(ids).stream().collect(Collectors.toMap(Product::getId, p -> p))));
    Map<Long, InventoryBatch> batchMap = itemList.stream()
        .map(InventoryOutboundSheetItem::getBatchId)
        .filter(Objects::nonNull)
        .distinct()
        .collect(Collectors.collectingAndThen(Collectors.toList(), ids ->
            ids.isEmpty() ? Map.of() : inventoryBatchMapper.selectBatchIds(ids).stream().collect(Collectors.toMap(InventoryBatch::getId, b -> b))));
    return Result.ok(toOutboundSheetResponse(sheet, itemList, productMap, batchMap));
  }

  @PostMapping("/outbound/sheet/confirm")
  @Transactional
  public Result<InventoryOutboundSheetResponse> confirmOutboundSheet(@RequestParam Long id) {
    Long orgId = AuthContext.getOrgId();
    InventoryOutboundSheet sheet = inventoryOutboundSheetMapper.selectById(id);
    if (sheet == null || sheet.getIsDeleted() == 1) {
      throw new IllegalArgumentException("领用单不存在");
    }
    if (orgId != null && !orgId.equals(sheet.getOrgId())) {
      throw new IllegalArgumentException("无权限访问该领用单");
    }
    if ("CONFIRMED".equalsIgnoreCase(sheet.getStatus())) {
      return Result.ok(getOutboundSheetDetail(id).getData());
    }
    List<InventoryOutboundSheetItem> items = inventoryOutboundSheetItemMapper.selectList(
        Wrappers.lambdaQuery(InventoryOutboundSheetItem.class)
            .eq(InventoryOutboundSheetItem::getIsDeleted, 0)
            .eq(InventoryOutboundSheetItem::getSheetId, id)
            .orderByAsc(InventoryOutboundSheetItem::getItemSort)
            .orderByAsc(InventoryOutboundSheetItem::getId));
    if (items.isEmpty()) {
      throw new IllegalArgumentException("领用单明细为空");
    }
    for (InventoryOutboundSheetItem item : items) {
      InventoryOutboundRequest outbound = new InventoryOutboundRequest();
      outbound.setOrgId(orgId);
      outbound.setOperatorStaffId(AuthContext.getStaffId());
      outbound.setProductId(item.getProductId());
      outbound.setWarehouseId(item.getWarehouseId());
      outbound.setBatchId(item.getBatchId());
      outbound.setQuantity(item.getQuantity());
      outbound.setReceiverName(sheet.getReceiverName());
      outbound.setReason(item.getReason());
      outbound.setOutboundNo(sheet.getOutboundNo());
      inventoryService.outbound(outbound);
    }
    sheet.setStatus("CONFIRMED");
    sheet.setConfirmStaffId(AuthContext.getStaffId());
    sheet.setConfirmTime(LocalDateTime.now());
    inventoryOutboundSheetMapper.updateById(sheet);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "INVENTORY_OUTBOUND_SHEET_CONFIRM", "OUTBOUND_SHEET", sheet.getId(), "确认出库:" + sheet.getOutboundNo());
    return Result.ok(getOutboundSheetDetail(id).getData());
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
      @RequestParam(required = false) String businessDomain,
      @RequestParam(required = false) String itemType,
      @RequestParam(required = false) Integer mallEnabled,
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "false") boolean lowStockOnly,
      @RequestParam(required = false) Integer expiryDays) {
    Long orgId = AuthContext.getOrgId();
    LambdaQueryWrapper<InventoryBatch> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(orgId != null, InventoryBatch::getOrgId, orgId)
        .eq(InventoryBatch::getIsDeleted, 0)
        .eq(productId != null, InventoryBatch::getProductId, productId)
        .eq(warehouseId != null, InventoryBatch::getWarehouseId, warehouseId);
    boolean hasProductAttrFilter = (category != null && !category.isBlank())
        || (businessDomain != null && !businessDomain.isBlank())
        || (itemType != null && !itemType.isBlank())
        || mallEnabled != null;
    if (hasProductAttrFilter) {
      List<Long> filteredProductIds = productMapper.selectList(
              Wrappers.lambdaQuery(Product.class)
                  .eq(Product::getIsDeleted, 0)
                  .eq(orgId != null, Product::getOrgId, orgId)
                  .like(category != null && !category.isBlank(), Product::getCategory, category)
                  .eq(businessDomain != null && !businessDomain.isBlank(), Product::getBusinessDomain, businessDomain)
                  .eq(itemType != null && !itemType.isBlank(), Product::getItemType, itemType)
                  .eq(mallEnabled != null, Product::getMallEnabled, mallEnabled))
          .stream()
          .map(Product::getId)
          .toList();
      if (filteredProductIds.isEmpty()) {
        return Result.ok(new Page<>(pageNo, pageSize, 0));
      }
      wrapper.in(InventoryBatch::getProductId, filteredProductIds);
    }
    if (keyword != null && !keyword.isBlank()) {
      List<Long> keywordProductIds = productMapper.selectList(
          Wrappers.lambdaQuery(Product.class)
              .eq(Product::getIsDeleted, 0)
              .eq(orgId != null, Product::getOrgId, orgId)
              .like(category != null && !category.isBlank(), Product::getCategory, category)
              .eq(businessDomain != null && !businessDomain.isBlank(), Product::getBusinessDomain, businessDomain)
              .eq(itemType != null && !itemType.isBlank(), Product::getItemType, itemType)
              .eq(mallEnabled != null, Product::getMallEnabled, mallEnabled)
              .and(p -> p.like(Product::getProductName, keyword)
                  .or().like(Product::getProductCode, keyword)))
          .stream()
          .map(Product::getId)
          .toList();
      if (keywordProductIds.isEmpty()) {
        wrapper.and(w -> w.like(InventoryBatch::getBatchNo, keyword));
      } else {
        wrapper.and(w -> w.like(InventoryBatch::getBatchNo, keyword)
            .or().in(InventoryBatch::getProductId, keywordProductIds));
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
      item.setBusinessDomain(product == null ? null : product.getBusinessDomain());
      item.setItemType(product == null ? null : product.getItemType());
      item.setMallEnabled(product == null ? null : product.getMallEnabled());
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
      item.setReceiverName(log.getReceiverName());
      item.setOutboundNo(log.getOutboundNo());
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
      @RequestParam(required = false) String inventoryType,
      @RequestParam(required = false) String adjustType,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo) {
    Long orgId = AuthContext.getOrgId();
    LambdaQueryWrapper<InventoryAdjustment> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(orgId != null, InventoryAdjustment::getOrgId, orgId)
        .eq(InventoryAdjustment::getIsDeleted, 0)
        .eq(productId != null, InventoryAdjustment::getProductId, productId)
        .eq(inventoryType != null && !inventoryType.isBlank(), InventoryAdjustment::getInventoryType, inventoryType)
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
      item.setInventoryType(adjust.getInventoryType());
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

  private String resolveOutboundNo(Long orgId, String requestNo) {
    String base = requestNo == null ? "" : requestNo.trim();
    if (!base.isBlank()) {
      return base;
    }
    String date = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    String generated = "LY-" + date;
    int suffix = 1;
    while (inventoryOutboundSheetMapper.selectCount(
        Wrappers.lambdaQuery(InventoryOutboundSheet.class)
            .eq(InventoryOutboundSheet::getIsDeleted, 0)
            .eq(orgId != null, InventoryOutboundSheet::getOrgId, orgId)
            .eq(InventoryOutboundSheet::getOutboundNo, generated)) > 0) {
      generated = "LY-" + date + "-" + suffix++;
    }
    return generated;
  }

  private InventoryOutboundSheetResponse toOutboundSheetResponse(
      InventoryOutboundSheet sheet,
      List<InventoryOutboundSheetItem> items,
      Map<Long, Product> productMap,
      Map<Long, InventoryBatch> batchMap) {
    InventoryOutboundSheetResponse response = new InventoryOutboundSheetResponse();
    response.setId(sheet.getId());
    response.setOutboundNo(sheet.getOutboundNo());
    response.setReceiverName(sheet.getReceiverName());
    response.setElderId(sheet.getElderId());
    response.setContractNo(sheet.getContractNo());
    response.setApplyDept(sheet.getApplyDept());
    response.setOperatorStaffId(sheet.getOperatorStaffId());
    response.setOperatorName(sheet.getOperatorName());
    response.setStatus(sheet.getStatus());
    response.setRemark(sheet.getRemark());
    response.setConfirmStaffId(sheet.getConfirmStaffId());
    response.setConfirmTime(sheet.getConfirmTime());
    response.setCreateTime(sheet.getCreateTime());
    List<InventoryOutboundSheetItemResponse> detail = (items == null ? List.<InventoryOutboundSheetItem>of() : items)
        .stream().map(item -> {
          InventoryOutboundSheetItemResponse row = new InventoryOutboundSheetItemResponse();
          row.setId(item.getId());
          row.setProductId(item.getProductId());
          Product product = productMap.get(item.getProductId());
          row.setProductName(product == null ? null : product.getProductName());
          row.setProductCode(product == null ? null : product.getProductCode());
          row.setUnit(product == null ? null : product.getUnit());
          row.setWarehouseId(item.getWarehouseId());
          row.setBatchId(item.getBatchId());
          InventoryBatch batch = item.getBatchId() == null ? null : batchMap.get(item.getBatchId());
          row.setBatchNo(batch == null ? null : batch.getBatchNo());
          row.setQuantity(item.getQuantity());
          row.setReason(item.getReason());
          return row;
        }).toList();
    response.setItems(detail);
    return response;
  }

  @GetMapping("/adjustment/diff-report")
  public Result<List<InventoryAdjustmentDiffItem>> adjustmentDiffReport(
      @RequestParam(required = false) Long warehouseId,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String inventoryType,
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
      if (inventoryType != null && !inventoryType.isBlank()
          && (row.getInventoryType() == null || !inventoryType.equalsIgnoreCase(row.getInventoryType()))) {
        continue;
      }
      String key = row.getProductId() + "-" + (rowWarehouseId == null ? "0" : rowWarehouseId);
      InventoryAdjustmentDiffItem item = resultMap.computeIfAbsent(key, k -> {
        InventoryAdjustmentDiffItem it = new InventoryAdjustmentDiffItem();
        it.setProductId(row.getProductId());
        it.setProductName(product == null ? null : product.getProductName());
        it.setCategory(rowCategory);
        it.setInventoryType(row.getInventoryType());
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
