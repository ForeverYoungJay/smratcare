package com.zhiyangyun.care.logistics.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderAdmission;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderDischarge;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderAdmissionMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderDischargeMapper;
import com.zhiyangyun.care.life.entity.DiningDeliveryRecord;
import com.zhiyangyun.care.life.entity.DiningMealOrder;
import com.zhiyangyun.care.life.entity.MaintenanceRequest;
import com.zhiyangyun.care.life.entity.RoomCleaningTask;
import com.zhiyangyun.care.life.mapper.DiningDeliveryRecordMapper;
import com.zhiyangyun.care.life.mapper.DiningMealOrderMapper;
import com.zhiyangyun.care.life.mapper.MaintenanceRequestMapper;
import com.zhiyangyun.care.life.mapper.RoomCleaningTaskMapper;
import com.zhiyangyun.care.logistics.entity.LogisticsEquipmentArchive;
import com.zhiyangyun.care.logistics.entity.LogisticsMaintenanceTodoJobLog;
import com.zhiyangyun.care.logistics.mapper.LogisticsEquipmentArchiveMapper;
import com.zhiyangyun.care.logistics.mapper.LogisticsMaintenanceTodoJobLogMapper;
import com.zhiyangyun.care.logistics.model.LogisticsNamedStatItem;
import com.zhiyangyun.care.logistics.model.LogisticsWorkbenchSummaryResponse;
import com.zhiyangyun.care.store.entity.InventoryAdjustment;
import com.zhiyangyun.care.store.entity.InventoryBatch;
import com.zhiyangyun.care.store.entity.InventoryLog;
import com.zhiyangyun.care.store.entity.MaterialPurchaseOrder;
import com.zhiyangyun.care.store.entity.MaterialPurchaseOrderItem;
import com.zhiyangyun.care.store.entity.Product;
import com.zhiyangyun.care.store.mapper.InventoryAdjustmentMapper;
import com.zhiyangyun.care.store.mapper.InventoryBatchMapper;
import com.zhiyangyun.care.store.mapper.InventoryLogMapper;
import com.zhiyangyun.care.store.mapper.MaterialPurchaseOrderItemMapper;
import com.zhiyangyun.care.store.mapper.MaterialPurchaseOrderMapper;
import com.zhiyangyun.care.store.mapper.ProductMapper;
import com.zhiyangyun.care.store.service.InventoryService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logistics/workbench")
public class LogisticsWorkbenchController {
  private final InventoryService inventoryService;
  private final InventoryLogMapper inventoryLogMapper;
  private final InventoryBatchMapper inventoryBatchMapper;
  private final ProductMapper productMapper;
  private final MaterialPurchaseOrderMapper materialPurchaseOrderMapper;
  private final MaterialPurchaseOrderItemMapper materialPurchaseOrderItemMapper;
  private final BedMapper bedMapper;
  private final RoomCleaningTaskMapper roomCleaningTaskMapper;
  private final ElderAdmissionMapper elderAdmissionMapper;
  private final ElderDischargeMapper elderDischargeMapper;
  private final MaintenanceRequestMapper maintenanceRequestMapper;
  private final DiningMealOrderMapper diningMealOrderMapper;
  private final DiningDeliveryRecordMapper diningDeliveryRecordMapper;
  private final InventoryAdjustmentMapper inventoryAdjustmentMapper;
  private final LogisticsEquipmentArchiveMapper logisticsEquipmentArchiveMapper;
  private final LogisticsMaintenanceTodoJobLogMapper logisticsMaintenanceTodoJobLogMapper;

  public LogisticsWorkbenchController(
      InventoryService inventoryService,
      InventoryLogMapper inventoryLogMapper,
      InventoryBatchMapper inventoryBatchMapper,
      ProductMapper productMapper,
      MaterialPurchaseOrderMapper materialPurchaseOrderMapper,
      MaterialPurchaseOrderItemMapper materialPurchaseOrderItemMapper,
      BedMapper bedMapper,
      RoomCleaningTaskMapper roomCleaningTaskMapper,
      ElderAdmissionMapper elderAdmissionMapper,
      ElderDischargeMapper elderDischargeMapper,
      MaintenanceRequestMapper maintenanceRequestMapper,
      DiningMealOrderMapper diningMealOrderMapper,
      DiningDeliveryRecordMapper diningDeliveryRecordMapper,
      InventoryAdjustmentMapper inventoryAdjustmentMapper,
      LogisticsEquipmentArchiveMapper logisticsEquipmentArchiveMapper,
      LogisticsMaintenanceTodoJobLogMapper logisticsMaintenanceTodoJobLogMapper) {
    this.inventoryService = inventoryService;
    this.inventoryLogMapper = inventoryLogMapper;
    this.inventoryBatchMapper = inventoryBatchMapper;
    this.productMapper = productMapper;
    this.materialPurchaseOrderMapper = materialPurchaseOrderMapper;
    this.materialPurchaseOrderItemMapper = materialPurchaseOrderItemMapper;
    this.bedMapper = bedMapper;
    this.roomCleaningTaskMapper = roomCleaningTaskMapper;
    this.elderAdmissionMapper = elderAdmissionMapper;
    this.elderDischargeMapper = elderDischargeMapper;
    this.maintenanceRequestMapper = maintenanceRequestMapper;
    this.diningMealOrderMapper = diningMealOrderMapper;
    this.diningDeliveryRecordMapper = diningDeliveryRecordMapper;
    this.inventoryAdjustmentMapper = inventoryAdjustmentMapper;
    this.logisticsEquipmentArchiveMapper = logisticsEquipmentArchiveMapper;
    this.logisticsMaintenanceTodoJobLogMapper = logisticsMaintenanceTodoJobLogMapper;
  }

  @GetMapping("/summary")
  public Result<LogisticsWorkbenchSummaryResponse> summary() {
    Long orgId = AuthContext.getOrgId();
    LocalDate today = LocalDate.now();
    LocalDate monthStart = today.withDayOfMonth(1);
    LocalDateTime todayStart = today.atStartOfDay();
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime monthStartAt = monthStart.atStartOfDay();
    LocalDateTime weekStartAt = today.minusDays(6).atStartOfDay();

    LogisticsWorkbenchSummaryResponse response = new LogisticsWorkbenchSummaryResponse();

    response.setLowStockCount(inventoryService.lowStockAlerts(orgId).size());
    response.setExpiringCount(inventoryService.expiryAlerts(orgId, 30).size());

    List<InventoryLog> allOutLogs = inventoryLogMapper.selectList(Wrappers.lambdaQuery(InventoryLog.class)
        .eq(InventoryLog::getIsDeleted, 0)
        .eq(orgId != null, InventoryLog::getOrgId, orgId)
        .eq(InventoryLog::getChangeType, "OUT")
        .ge(InventoryLog::getCreateTime, monthStartAt));

    response.setTodayOutboundQty(allOutLogs.stream()
        .filter(log -> log.getCreateTime() != null && !log.getCreateTime().isBefore(todayStart))
        .mapToLong(log -> log.getChangeQty() == null ? 0 : log.getChangeQty())
        .sum());

    List<InventoryBatch> allBatches = inventoryBatchMapper.selectList(Wrappers.lambdaQuery(InventoryBatch.class)
        .eq(InventoryBatch::getIsDeleted, 0)
        .eq(orgId != null, InventoryBatch::getOrgId, orgId));
    Map<Long, BigDecimal> batchCostMap = new HashMap<>();
    for (InventoryBatch batch : allBatches) {
      if (batch.getId() == null) continue;
      batchCostMap.put(batch.getId(), batch.getCostPrice() == null ? BigDecimal.ZERO : batch.getCostPrice());
    }
    BigDecimal inventoryTotalAmount = BigDecimal.ZERO;
    for (InventoryBatch batch : allBatches) {
      int qty = batch.getQuantity() == null ? 0 : batch.getQuantity();
      BigDecimal cost = batch.getCostPrice() == null ? BigDecimal.ZERO : batch.getCostPrice();
      inventoryTotalAmount = inventoryTotalAmount.add(cost.multiply(BigDecimal.valueOf(qty)));
    }
    response.setInventoryTotalAmount(inventoryTotalAmount);

    Map<Long, Product> productMap = productMapper.selectList(Wrappers.lambdaQuery(Product.class)
            .eq(Product::getIsDeleted, 0)
            .eq(orgId != null, Product::getOrgId, orgId))
        .stream().collect(java.util.stream.Collectors.toMap(Product::getId, item -> item));

    long inventoryAssetQty = 0;
    long inventoryConsumableQty = 0;
    long inventoryFoodQty = 0;
    long inventoryServiceQty = 0;
    for (InventoryBatch batch : allBatches) {
      int qty = batch.getQuantity() == null ? 0 : batch.getQuantity();
      Product product = batch.getProductId() == null ? null : productMap.get(batch.getProductId());
      String itemType = normalizeItemType(product == null ? null : product.getItemType());
      if ("ASSET".equals(itemType)) {
        inventoryAssetQty += qty;
      } else if ("FOOD".equals(itemType)) {
        inventoryFoodQty += qty;
      } else if ("SERVICE".equals(itemType)) {
        inventoryServiceQty += qty;
      } else {
        inventoryConsumableQty += qty;
      }
    }
    response.setInventoryAssetQty(inventoryAssetQty);
    response.setInventoryConsumableQty(inventoryConsumableQty);
    response.setInventoryFoodQty(inventoryFoodQty);
    response.setInventoryServiceQty(inventoryServiceQty);

    long lowStockAssetCount = 0;
    long lowStockConsumableCount = 0;
    long lowStockFoodCount = 0;
    long lowStockServiceCount = 0;
    for (var alert : inventoryService.lowStockAlerts(orgId)) {
      Product product = alert.getProductId() == null ? null : productMap.get(alert.getProductId());
      String itemType = normalizeItemType(product == null ? null : product.getItemType());
      if ("ASSET".equals(itemType)) {
        lowStockAssetCount++;
      } else if ("FOOD".equals(itemType)) {
        lowStockFoodCount++;
      } else if ("SERVICE".equals(itemType)) {
        lowStockServiceCount++;
      } else {
        lowStockConsumableCount++;
      }
    }
    response.setLowStockAssetCount(lowStockAssetCount);
    response.setLowStockConsumableCount(lowStockConsumableCount);
    response.setLowStockFoodCount(lowStockFoodCount);
    response.setLowStockServiceCount(lowStockServiceCount);

    Map<Long, Long> weekConsumeMap = new HashMap<>();
    for (InventoryLog log : allOutLogs) {
      if (log.getCreateTime() == null || log.getCreateTime().isBefore(weekStartAt) || log.getProductId() == null) continue;
      weekConsumeMap.merge(log.getProductId(), (long) (log.getChangeQty() == null ? 0 : log.getChangeQty()), Long::sum);
    }
    response.setWeekTopConsumption(toTopNamedItems(weekConsumeMap, productMap, 5));

    response.setPurchasePendingApprovalCount(materialPurchaseOrderMapper.selectCount(Wrappers.lambdaQuery(MaterialPurchaseOrder.class)
        .eq(MaterialPurchaseOrder::getIsDeleted, 0)
        .eq(orgId != null, MaterialPurchaseOrder::getOrgId, orgId)
        .eq(MaterialPurchaseOrder::getStatus, "DRAFT")));
    response.setPurchaseApprovedNotArrivedCount(materialPurchaseOrderMapper.selectCount(Wrappers.lambdaQuery(MaterialPurchaseOrder.class)
        .eq(MaterialPurchaseOrder::getIsDeleted, 0)
        .eq(orgId != null, MaterialPurchaseOrder::getOrgId, orgId)
        .eq(MaterialPurchaseOrder::getStatus, "APPROVED")));
    response.setPurchaseReceivedNotSettledCount(materialPurchaseOrderMapper.selectCount(Wrappers.lambdaQuery(MaterialPurchaseOrder.class)
        .eq(MaterialPurchaseOrder::getIsDeleted, 0)
        .eq(orgId != null, MaterialPurchaseOrder::getOrgId, orgId)
        .eq(MaterialPurchaseOrder::getStatus, "COMPLETED")));

    BigDecimal monthPurchaseAmount = materialPurchaseOrderMapper.selectList(Wrappers.lambdaQuery(MaterialPurchaseOrder.class)
            .eq(MaterialPurchaseOrder::getIsDeleted, 0)
            .eq(orgId != null, MaterialPurchaseOrder::getOrgId, orgId)
            .between(MaterialPurchaseOrder::getOrderDate, monthStart, today)
            .ne(MaterialPurchaseOrder::getStatus, "CANCELLED"))
        .stream()
        .map(order -> order.getTotalAmount() == null ? BigDecimal.ZERO : order.getTotalAmount())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    response.setMonthPurchaseAmount(monthPurchaseAmount);

    List<MaterialPurchaseOrder> monthPurchaseOrders = materialPurchaseOrderMapper.selectList(
        Wrappers.lambdaQuery(MaterialPurchaseOrder.class)
            .eq(MaterialPurchaseOrder::getIsDeleted, 0)
            .eq(orgId != null, MaterialPurchaseOrder::getOrgId, orgId)
            .between(MaterialPurchaseOrder::getOrderDate, monthStart, today)
            .ne(MaterialPurchaseOrder::getStatus, "CANCELLED"));
    BigDecimal monthPurchaseAssetAmount = BigDecimal.ZERO;
    BigDecimal monthPurchaseConsumableAmount = BigDecimal.ZERO;
    BigDecimal monthPurchaseFoodAmount = BigDecimal.ZERO;
    BigDecimal monthPurchaseServiceAmount = BigDecimal.ZERO;
    if (!monthPurchaseOrders.isEmpty()) {
      List<Long> purchaseOrderIds = monthPurchaseOrders.stream()
          .map(MaterialPurchaseOrder::getId)
          .filter(java.util.Objects::nonNull)
          .toList();
      List<MaterialPurchaseOrderItem> purchaseItems = materialPurchaseOrderItemMapper.selectList(
          Wrappers.lambdaQuery(MaterialPurchaseOrderItem.class)
              .eq(MaterialPurchaseOrderItem::getIsDeleted, 0)
              .in(MaterialPurchaseOrderItem::getOrderId, purchaseOrderIds));
      for (MaterialPurchaseOrderItem item : purchaseItems) {
        BigDecimal amount = item.getAmount() == null ? BigDecimal.ZERO : item.getAmount();
        Product product = item.getProductId() == null ? null : productMap.get(item.getProductId());
        String itemType = normalizeItemType(product == null ? null : product.getItemType());
        if ("ASSET".equals(itemType)) {
          monthPurchaseAssetAmount = monthPurchaseAssetAmount.add(amount);
        } else if ("FOOD".equals(itemType)) {
          monthPurchaseFoodAmount = monthPurchaseFoodAmount.add(amount);
        } else if ("SERVICE".equals(itemType)) {
          monthPurchaseServiceAmount = monthPurchaseServiceAmount.add(amount);
        } else {
          monthPurchaseConsumableAmount = monthPurchaseConsumableAmount.add(amount);
        }
      }
    }
    response.setMonthPurchaseAssetAmount(monthPurchaseAssetAmount);
    response.setMonthPurchaseConsumableAmount(monthPurchaseConsumableAmount);
    response.setMonthPurchaseFoodAmount(monthPurchaseFoodAmount);
    response.setMonthPurchaseServiceAmount(monthPurchaseServiceAmount);

    response.setFreeBeds(bedMapper.selectCount(Wrappers.lambdaQuery(Bed.class)
        .eq(Bed::getIsDeleted, 0)
        .eq(orgId != null, Bed::getOrgId, orgId)
        .eq(Bed::getStatus, 1)));
    response.setOccupiedBeds(bedMapper.selectCount(Wrappers.lambdaQuery(Bed.class)
        .eq(Bed::getIsDeleted, 0)
        .eq(orgId != null, Bed::getOrgId, orgId)
        .eq(Bed::getStatus, 2)));
    response.setMaintenanceBeds(bedMapper.selectCount(Wrappers.lambdaQuery(Bed.class)
        .eq(Bed::getIsDeleted, 0)
        .eq(orgId != null, Bed::getOrgId, orgId)
        .eq(Bed::getStatus, 3)));

    List<RoomCleaningTask> todayCleaningTasks = roomCleaningTaskMapper.selectList(Wrappers.lambdaQuery(RoomCleaningTask.class)
        .eq(RoomCleaningTask::getIsDeleted, 0)
        .eq(orgId != null, RoomCleaningTask::getOrgId, orgId)
        .eq(RoomCleaningTask::getPlanDate, today)
        .ne(RoomCleaningTask::getStatus, "DONE"));
    List<Long> cleaningRoomIds = todayCleaningTasks.stream()
        .map(RoomCleaningTask::getRoomId)
        .filter(java.util.Objects::nonNull)
        .distinct()
        .toList();
    long cleaningBeds = cleaningRoomIds.isEmpty() ? 0 : bedMapper.selectCount(Wrappers.lambdaQuery(Bed.class)
        .eq(Bed::getIsDeleted, 0)
        .eq(orgId != null, Bed::getOrgId, orgId)
        .in(Bed::getRoomId, cleaningRoomIds));
    response.setCleaningBeds(cleaningBeds);

    response.setTodayAdmissionCount(elderAdmissionMapper.selectCount(Wrappers.lambdaQuery(ElderAdmission.class)
        .eq(ElderAdmission::getIsDeleted, 0)
        .eq(orgId != null, ElderAdmission::getOrgId, orgId)
        .eq(ElderAdmission::getAdmissionDate, today)));
    response.setTodayDischargeCount(elderDischargeMapper.selectCount(Wrappers.lambdaQuery(ElderDischarge.class)
        .eq(ElderDischarge::getIsDeleted, 0)
        .eq(orgId != null, ElderDischarge::getOrgId, orgId)
        .eq(ElderDischarge::getDischargeDate, today)));

    response.setMaintenancePendingCount(maintenanceRequestMapper.selectCount(Wrappers.lambdaQuery(MaintenanceRequest.class)
        .eq(MaintenanceRequest::getIsDeleted, 0)
        .eq(orgId != null, MaintenanceRequest::getOrgId, orgId)
        .eq(MaintenanceRequest::getStatus, "OPEN")));
    response.setMaintenanceProcessingCount(maintenanceRequestMapper.selectCount(Wrappers.lambdaQuery(MaintenanceRequest.class)
        .eq(MaintenanceRequest::getIsDeleted, 0)
        .eq(orgId != null, MaintenanceRequest::getOrgId, orgId)
        .eq(MaintenanceRequest::getStatus, "PROCESSING")));
    response.setMaintenanceOverdueCount(maintenanceRequestMapper.selectCount(Wrappers.lambdaQuery(MaintenanceRequest.class)
        .eq(MaintenanceRequest::getIsDeleted, 0)
        .eq(orgId != null, MaintenanceRequest::getOrgId, orgId)
        .in(MaintenanceRequest::getStatus, List.of("OPEN", "PROCESSING"))
        .lt(MaintenanceRequest::getReportedAt, now.minusDays(2))));
    BigDecimal monthMaintenanceCost = maintenanceRequestMapper.selectList(Wrappers.lambdaQuery(MaintenanceRequest.class)
            .eq(MaintenanceRequest::getIsDeleted, 0)
            .eq(orgId != null, MaintenanceRequest::getOrgId, orgId)
            .ge(MaintenanceRequest::getReportedAt, monthStartAt))
        .stream()
        .map(item -> item.getTotalCost() == null ? BigDecimal.ZERO : item.getTotalCost())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    response.setMonthMaintenanceCost(monthMaintenanceCost);

    List<DiningMealOrder> todayMealOrders = diningMealOrderMapper.selectList(Wrappers.lambdaQuery(DiningMealOrder.class)
        .eq(DiningMealOrder::getIsDeleted, 0)
        .eq(orgId != null, DiningMealOrder::getOrgId, orgId)
        .eq(DiningMealOrder::getOrderDate, today));
    response.setTodayMealOrderCount(todayMealOrders.size());
    response.setPersonalizedMealCount(todayMealOrders.stream()
        .filter(item -> item.getOverrideId() != null)
        .count());
    long todayDelivered = todayMealOrders.stream().filter(item -> "DELIVERED".equals(item.getStatus())).count();
    long todayUndelivered = Math.max(todayMealOrders.size() - todayDelivered, 0);
    response.setUndeliveredCount(todayUndelivered);
    response.setDeliveryCompletionRate(todayMealOrders.isEmpty()
        ? BigDecimal.ZERO
        : BigDecimal.valueOf(todayDelivered)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(todayMealOrders.size()), 2, RoundingMode.HALF_UP));
    response.setTodayDiningCost(todayMealOrders.stream()
        .map(item -> item.getTotalAmount() == null ? BigDecimal.ZERO : item.getTotalAmount())
        .reduce(BigDecimal.ZERO, BigDecimal::add));

    response.setDiabetesMealCount(todayMealOrders.stream()
        .filter(item -> containsAny(item.getDishNames(), "糖尿"))
        .count());
    response.setLowSaltMealCount(todayMealOrders.stream()
        .filter(item -> containsAny(item.getDishNames(), "低盐"))
        .count());
    response.setAllergyAlertCount(todayMealOrders.stream()
        .filter(item -> containsAny(item.getRemark(), "过敏", "禁忌"))
        .count());
    response.setSpecialNutritionMealCount(todayMealOrders.stream()
        .filter(item -> containsAny(item.getDishNames(), "营养", "软食", "流食", "特殊"))
        .count());

    BigDecimal monthConsumeAmount = BigDecimal.ZERO;
    BigDecimal nursingConsumeAmount = BigDecimal.ZERO;
    BigDecimal diningConsumeAmount = BigDecimal.ZERO;
    long todayOutboundAssetQty = 0;
    long todayOutboundConsumableQty = 0;
    long todayOutboundFoodQty = 0;
    long todayOutboundServiceQty = 0;
    Map<Long, Long> monthConsumeMap = new HashMap<>();
    for (InventoryLog log : allOutLogs) {
      int qty = log.getChangeQty() == null ? 0 : log.getChangeQty();
      BigDecimal unitCost = log.getBatchId() == null ? BigDecimal.ZERO : batchCostMap.getOrDefault(log.getBatchId(), BigDecimal.ZERO);
      BigDecimal amount = unitCost.multiply(BigDecimal.valueOf(qty));
      monthConsumeAmount = monthConsumeAmount.add(amount);
      if ("CONSUME".equals(log.getBizType())) nursingConsumeAmount = nursingConsumeAmount.add(amount);
      if ("ORDER".equals(log.getBizType())) diningConsumeAmount = diningConsumeAmount.add(amount);
      if (log.getProductId() != null) {
        monthConsumeMap.merge(log.getProductId(), (long) qty, Long::sum);
        if (log.getCreateTime() != null && !log.getCreateTime().isBefore(todayStart)) {
          Product product = productMap.get(log.getProductId());
          String itemType = normalizeItemType(product == null ? null : product.getItemType());
          if ("ASSET".equals(itemType)) {
            todayOutboundAssetQty += qty;
          } else if ("FOOD".equals(itemType)) {
            todayOutboundFoodQty += qty;
          } else if ("SERVICE".equals(itemType)) {
            todayOutboundServiceQty += qty;
          } else {
            todayOutboundConsumableQty += qty;
          }
        }
      }
    }
    response.setMonthConsumeAmount(monthConsumeAmount);
    response.setNursingConsumeAmount(nursingConsumeAmount);
    response.setDiningConsumeAmount(diningConsumeAmount);
    response.setMonthTopConsumption(toTopNamedItems(monthConsumeMap, productMap, 10));
    response.setTodayOutboundAssetQty(todayOutboundAssetQty);
    response.setTodayOutboundConsumableQty(todayOutboundConsumableQty);
    response.setTodayOutboundFoodQty(todayOutboundFoodQty);
    response.setTodayOutboundServiceQty(todayOutboundServiceQty);

    response.setTodayCleaningTaskCount(roomCleaningTaskMapper.selectCount(Wrappers.lambdaQuery(RoomCleaningTask.class)
        .eq(RoomCleaningTask::getIsDeleted, 0)
        .eq(orgId != null, RoomCleaningTask::getOrgId, orgId)
        .eq(RoomCleaningTask::getPlanDate, today)));
    response.setTodayMaintenanceTaskCount(maintenanceRequestMapper.selectCount(Wrappers.lambdaQuery(MaintenanceRequest.class)
        .eq(MaintenanceRequest::getIsDeleted, 0)
        .eq(orgId != null, MaintenanceRequest::getOrgId, orgId)
        .ge(MaintenanceRequest::getReportedAt, todayStart)));
    response.setTodayDeliveryTaskCount(diningDeliveryRecordMapper.selectCount(Wrappers.lambdaQuery(DiningDeliveryRecord.class)
        .eq(DiningDeliveryRecord::getIsDeleted, 0)
        .eq(orgId != null, DiningDeliveryRecord::getOrgId, orgId)
        .ge(DiningDeliveryRecord::getCreateTime, todayStart)));
    response.setTodayInventoryCheckTaskCount(inventoryAdjustmentMapper.selectCount(Wrappers.lambdaQuery(InventoryAdjustment.class)
        .eq(InventoryAdjustment::getIsDeleted, 0)
        .eq(orgId != null, InventoryAdjustment::getOrgId, orgId)
        .ge(InventoryAdjustment::getCreateTime, todayStart)));

    response.setEquipmentTotalCount(logisticsEquipmentArchiveMapper.selectCount(Wrappers.lambdaQuery(LogisticsEquipmentArchive.class)
        .eq(LogisticsEquipmentArchive::getIsDeleted, 0)
        .eq(orgId != null, LogisticsEquipmentArchive::getOrgId, orgId)));
    response.setEquipmentMaintainingCount(logisticsEquipmentArchiveMapper.selectCount(Wrappers.lambdaQuery(LogisticsEquipmentArchive.class)
        .eq(LogisticsEquipmentArchive::getIsDeleted, 0)
        .eq(orgId != null, LogisticsEquipmentArchive::getOrgId, orgId)
        .eq(LogisticsEquipmentArchive::getStatus, "MAINTENANCE")));
    response.setEquipmentDueSoonCount(logisticsEquipmentArchiveMapper.selectCount(Wrappers.lambdaQuery(LogisticsEquipmentArchive.class)
        .eq(LogisticsEquipmentArchive::getIsDeleted, 0)
        .eq(orgId != null, LogisticsEquipmentArchive::getOrgId, orgId)
        .isNotNull(LogisticsEquipmentArchive::getNextMaintainedAt)
        .between(LogisticsEquipmentArchive::getNextMaintainedAt, now, now.plusDays(30))));

    LogisticsMaintenanceTodoJobLog latestJobLog = logisticsMaintenanceTodoJobLogMapper.selectOne(
        Wrappers.lambdaQuery(LogisticsMaintenanceTodoJobLog.class)
            .eq(LogisticsMaintenanceTodoJobLog::getIsDeleted, 0)
            .and(orgId != null, w -> w.eq(LogisticsMaintenanceTodoJobLog::getOrgId, orgId).or().isNull(LogisticsMaintenanceTodoJobLog::getOrgId))
            .orderByDesc(LogisticsMaintenanceTodoJobLog::getExecutedAt)
            .orderByDesc(LogisticsMaintenanceTodoJobLog::getCreateTime)
            .last("LIMIT 1"));
    if (latestJobLog != null) {
      response.setMaintenanceTodoLastStatus(latestJobLog.getStatus());
      response.setMaintenanceTodoLastTriggerType(latestJobLog.getTriggerType());
      response.setMaintenanceTodoLastExecutedAt(latestJobLog.getExecutedAt());
      response.setMaintenanceTodoLastCreatedCount(latestJobLog.getCreatedCount());
      response.setMaintenanceTodoLastSkippedCount(latestJobLog.getSkippedCount());
      response.setMaintenanceTodoLastErrorMessage(latestJobLog.getErrorMessage());
    }
    response.setMaintenanceTodoFailedCount7d(logisticsMaintenanceTodoJobLogMapper.selectCount(
        Wrappers.lambdaQuery(LogisticsMaintenanceTodoJobLog.class)
            .eq(LogisticsMaintenanceTodoJobLog::getIsDeleted, 0)
            .and(orgId != null, w -> w.eq(LogisticsMaintenanceTodoJobLog::getOrgId, orgId).or().isNull(LogisticsMaintenanceTodoJobLog::getOrgId))
            .eq(LogisticsMaintenanceTodoJobLog::getStatus, "FAILED")
            .ge(LogisticsMaintenanceTodoJobLog::getExecutedAt, now.minusDays(7))));

    return Result.ok(response);
  }

  private List<LogisticsNamedStatItem> toTopNamedItems(
      Map<Long, Long> countMap,
      Map<Long, Product> productMap,
      int limit) {
    if (countMap.isEmpty()) {
      return List.of();
    }
    List<LogisticsNamedStatItem> items = new ArrayList<>();
    countMap.entrySet().stream()
        .sorted(Map.Entry.<Long, Long>comparingByValue(Comparator.reverseOrder()))
        .limit(limit)
        .forEach(entry -> {
          Product product = productMap.get(entry.getKey());
          LogisticsNamedStatItem item = new LogisticsNamedStatItem();
          item.setName(product == null ? ("物资#" + entry.getKey()) : product.getProductName());
          item.setQuantity(entry.getValue());
          items.add(item);
        });
    return items;
  }

  private boolean containsAny(String text, String... keywords) {
    if (text == null || text.isBlank() || keywords == null || keywords.length == 0) {
      return false;
    }
    for (String keyword : keywords) {
      if (keyword != null && !keyword.isBlank() && text.contains(keyword)) {
        return true;
      }
    }
    return false;
  }

  private String normalizeItemType(String itemType) {
    if (itemType == null || itemType.isBlank()) {
      return "CONSUMABLE";
    }
    return itemType.trim().toUpperCase();
  }
}
