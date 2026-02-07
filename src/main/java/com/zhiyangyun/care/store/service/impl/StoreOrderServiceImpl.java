package com.zhiyangyun.care.store.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.store.entity.Disease;
import com.zhiyangyun.care.store.entity.DiseaseForbiddenTag;
import com.zhiyangyun.care.store.entity.ElderDisease;
import com.zhiyangyun.care.store.entity.ElderPointsAccount;
import com.zhiyangyun.care.store.entity.ElderProfile;
import com.zhiyangyun.care.store.entity.InventoryBatch;
import com.zhiyangyun.care.store.entity.InventoryLog;
import com.zhiyangyun.care.store.entity.InventoryBatch;
import com.zhiyangyun.care.store.entity.OrderItem;
import com.zhiyangyun.care.store.entity.PointsLog;
import com.zhiyangyun.care.store.entity.Product;
import com.zhiyangyun.care.store.entity.ProductTag;
import com.zhiyangyun.care.store.entity.StoreOrder;
import com.zhiyangyun.care.store.mapper.DiseaseForbiddenTagMapper;
import com.zhiyangyun.care.store.mapper.DiseaseMapper;
import com.zhiyangyun.care.store.mapper.ElderDiseaseMapper;
import com.zhiyangyun.care.store.mapper.ElderPointsAccountMapper;
import com.zhiyangyun.care.store.mapper.ElderProfileMapper;
import com.zhiyangyun.care.store.mapper.InventoryBatchMapper;
import com.zhiyangyun.care.store.mapper.InventoryLogMapper;
import com.zhiyangyun.care.store.mapper.OrderItemMapper;
import com.zhiyangyun.care.store.mapper.PointsLogMapper;
import com.zhiyangyun.care.store.mapper.ProductMapper;
import com.zhiyangyun.care.store.mapper.ProductTagMapper;
import com.zhiyangyun.care.store.mapper.StoreOrderMapper;
import com.zhiyangyun.care.store.mapper.InventoryBatchMapper;
import com.zhiyangyun.care.store.model.ForbiddenReason;
import com.zhiyangyun.care.store.model.OrderCancelRequest;
import com.zhiyangyun.care.store.model.OrderPreviewRequest;
import com.zhiyangyun.care.store.model.OrderPreviewResponse;
import com.zhiyangyun.care.store.model.OrderStatus;
import com.zhiyangyun.care.store.model.OrderRefundRequest;
import com.zhiyangyun.care.store.model.OrderSubmitResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoreOrderServiceImpl implements com.zhiyangyun.care.store.service.StoreOrderService {
  private final ElderProfileMapper elderMapper;
  private final ElderDiseaseMapper elderDiseaseMapper;
  private final DiseaseMapper diseaseMapper;
  private final DiseaseForbiddenTagMapper forbiddenTagMapper;
  private final ProductMapper productMapper;
  private final ProductTagMapper productTagMapper;
  private final ElderPointsAccountMapper pointsAccountMapper;
  private final PointsLogMapper pointsLogMapper;
  private final StoreOrderMapper orderMapper;
  private final OrderItemMapper orderItemMapper;
  private final InventoryLogMapper inventoryLogMapper;
  private final InventoryBatchMapper inventoryBatchMapper;

  public StoreOrderServiceImpl(
      ElderProfileMapper elderMapper,
      ElderDiseaseMapper elderDiseaseMapper,
      DiseaseMapper diseaseMapper,
      DiseaseForbiddenTagMapper forbiddenTagMapper,
      ProductMapper productMapper,
      ProductTagMapper productTagMapper,
      ElderPointsAccountMapper pointsAccountMapper,
      PointsLogMapper pointsLogMapper,
      StoreOrderMapper orderMapper,
      OrderItemMapper orderItemMapper,
      InventoryLogMapper inventoryLogMapper,
      InventoryBatchMapper inventoryBatchMapper) {
    this.elderMapper = elderMapper;
    this.elderDiseaseMapper = elderDiseaseMapper;
    this.diseaseMapper = diseaseMapper;
    this.forbiddenTagMapper = forbiddenTagMapper;
    this.productMapper = productMapper;
    this.productTagMapper = productTagMapper;
    this.pointsAccountMapper = pointsAccountMapper;
    this.pointsLogMapper = pointsLogMapper;
    this.orderMapper = orderMapper;
    this.orderItemMapper = orderItemMapper;
    this.inventoryLogMapper = inventoryLogMapper;
    this.inventoryBatchMapper = inventoryBatchMapper;
  }

  @Override
  public OrderPreviewResponse preview(OrderPreviewRequest request) {
    OrderPreviewResponse response = new OrderPreviewResponse();
    response.setElderId(request.getElderId());
    response.setProductId(request.getProductId());
    response.setQty(request.getQty());

    ElderProfile elder = elderMapper.selectById(request.getElderId());
    if (elder == null) {
      response.setStatus(OrderStatus.ELDER_NOT_FOUND.name());
      response.setAllowed(false);
      response.setMessage("Elder not found");
      return response;
    }

    Product product = productMapper.selectById(request.getProductId());
    if (product == null
        || product.getStatus() == null
        || product.getStatus() != 1
        || !elder.getOrgId().equals(product.getOrgId())) {
      response.setStatus(OrderStatus.PRODUCT_NOT_FOUND.name());
      response.setAllowed(false);
      response.setMessage("Product not found or disabled");
      return response;
    }

    response.setProductName(product.getProductName());
    int pointsRequired = (product.getPointsPrice() == null ? 0 : product.getPointsPrice()) * request.getQty();
    response.setPointsRequired(pointsRequired);

    Set<Long> productTagIds = parseTagIds(product.getTagIds());
    if (productTagIds.isEmpty()) {
      response.setStatus(OrderStatus.ALLOWED.name());
      response.setAllowed(true);
      response.setMessage("Allowed");
      return response;
    }

    List<ElderDisease> elderDiseases = elderDiseaseMapper.selectList(
        Wrappers.lambdaQuery(ElderDisease.class)
            .eq(ElderDisease::getOrgId, elder.getOrgId())
            .eq(ElderDisease::getElderId, elder.getId()));

    if (elderDiseases.isEmpty()) {
      response.setStatus(OrderStatus.ALLOWED.name());
      response.setAllowed(true);
      response.setMessage("Allowed");
      return response;
    }

    List<Long> diseaseIds = elderDiseases.stream().map(ElderDisease::getDiseaseId).collect(Collectors.toList());
    List<DiseaseForbiddenTag> forbiddenTags = forbiddenTagMapper.selectList(
        Wrappers.lambdaQuery(DiseaseForbiddenTag.class)
            .eq(DiseaseForbiddenTag::getOrgId, elder.getOrgId())
            .in(DiseaseForbiddenTag::getDiseaseId, diseaseIds));

    if (forbiddenTags.isEmpty()) {
      response.setStatus(OrderStatus.ALLOWED.name());
      response.setAllowed(true);
      response.setMessage("Allowed");
      return response;
    }

    Set<Long> forbiddenTagIds = forbiddenTags.stream()
        .map(DiseaseForbiddenTag::getTagId)
        .collect(Collectors.toSet());

    Set<Long> hitTagIds = new HashSet<>();
    for (Long tagId : productTagIds) {
      if (forbiddenTagIds.contains(tagId)) {
        hitTagIds.add(tagId);
      }
    }

    if (hitTagIds.isEmpty()) {
      response.setStatus(OrderStatus.ALLOWED.name());
      response.setAllowed(true);
      response.setMessage("Allowed");
      return response;
    }

    Map<Long, Disease> diseaseMap = new HashMap<>();
    for (Long diseaseId : diseaseIds) {
      Disease disease = diseaseMapper.selectOne(
          Wrappers.lambdaQuery(Disease.class)
              .eq(Disease::getOrgId, elder.getOrgId())
              .eq(Disease::getId, diseaseId));
      diseaseMap.put(diseaseId, disease);
    }

    Map<Long, ProductTag> tagMap = new HashMap<>();
    for (Long tagId : hitTagIds) {
      ProductTag tag = productTagMapper.selectOne(
          Wrappers.lambdaQuery(ProductTag.class)
              .eq(ProductTag::getOrgId, elder.getOrgId())
              .eq(ProductTag::getId, tagId));
      tagMap.put(tagId, tag);
    }

    List<ForbiddenReason> reasons = new ArrayList<>();
    for (DiseaseForbiddenTag forbiddenTag : forbiddenTags) {
      if (!hitTagIds.contains(forbiddenTag.getTagId())) {
        continue;
      }
      Disease disease = diseaseMap.get(forbiddenTag.getDiseaseId());
      ProductTag tag = tagMap.get(forbiddenTag.getTagId());
      ForbiddenReason reason = new ForbiddenReason();
      reason.setDiseaseId(forbiddenTag.getDiseaseId());
      reason.setDiseaseName(disease != null ? disease.getDiseaseName() : null);
      reason.setTagId(forbiddenTag.getTagId());
      reason.setTagCode(tag != null ? tag.getTagCode() : null);
      reason.setTagName(tag != null ? tag.getTagName() : null);
      reasons.add(reason);
    }

    response.setStatus(OrderStatus.BLOCKED.name());
    response.setAllowed(false);
    response.setReasons(reasons);
    response.setMessage("Blocked by health risk rules");
    return response;
  }

  @Override
  @Transactional
  public OrderSubmitResponse submit(OrderPreviewRequest request) {
    OrderPreviewResponse preview = preview(request);
    OrderSubmitResponse response = new OrderSubmitResponse();
    response.setPreview(preview);

    if (!preview.isAllowed()) {
      response.setStatus(preview.getStatus());
      response.setAllowed(false);
      response.setMessage(preview.getMessage());
      return response;
    }

    ElderProfile elder = elderMapper.selectById(request.getElderId());
    if (elder == null) {
      response.setStatus(OrderStatus.ELDER_NOT_FOUND.name());
      response.setAllowed(false);
      response.setMessage("Elder not found");
      return response;
    }

    Product product = productMapper.selectById(request.getProductId());
    if (product == null || !elder.getOrgId().equals(product.getOrgId())) {
      response.setStatus(OrderStatus.PRODUCT_NOT_FOUND.name());
      response.setAllowed(false);
      response.setMessage("Product not found");
      return response;
    }

    ElderPointsAccount account = pointsAccountMapper.selectOne(
        Wrappers.lambdaQuery(ElderPointsAccount.class)
            .eq(ElderPointsAccount::getOrgId, elder.getOrgId())
            .eq(ElderPointsAccount::getElderId, elder.getId()));
    if (account == null) {
      response.setStatus(OrderStatus.INSUFFICIENT_POINTS.name());
      response.setAllowed(false);
      response.setMessage("Points account not found");
      return response;
    }

    int pointsRequired = preview.getPointsRequired() == null ? 0 : preview.getPointsRequired();
    if (account.getPointsBalance() == null || account.getPointsBalance() < pointsRequired) {
      response.setStatus(OrderStatus.INSUFFICIENT_POINTS.name());
      response.setAllowed(false);
      response.setMessage("Insufficient points");
      return response;
    }

    if (!hasSufficientInventory(elder.getOrgId(), product.getId(), request.getQty())) {
      response.setStatus(OrderStatus.INSUFFICIENT_STOCK.name());
      response.setAllowed(false);
      response.setMessage("Insufficient stock");
      return response;
    }

    String orderNo = generateOrderNo();
    BigDecimal unitPrice = product.getPrice() == null ? BigDecimal.ZERO : product.getPrice();
    BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(request.getQty()));

    StoreOrder order = new StoreOrder();
    order.setOrgId(elder.getOrgId());
    order.setOrderNo(orderNo);
    order.setElderId(elder.getId());
    order.setTotalAmount(totalAmount);
    order.setPointsUsed(pointsRequired);
    order.setPayableAmount(totalAmount);
    order.setPayStatus(1);
    order.setPayTime(LocalDateTime.now());
    order.setOrderStatus(1);
    orderMapper.insert(order);

    OrderItem item = new OrderItem();
    item.setOrgId(elder.getOrgId());
    item.setOrderId(order.getId());
    item.setProductId(product.getId());
    item.setProductName(product.getProductName());
    item.setUnitPrice(unitPrice);
    item.setQuantity(request.getQty());
    item.setTotalAmount(totalAmount);
    orderItemMapper.insert(item);

    int balanceAfter = account.getPointsBalance() - pointsRequired;
    account.setPointsBalance(balanceAfter);
    pointsAccountMapper.updateById(account);

    PointsLog log = new PointsLog();
    log.setOrgId(elder.getOrgId());
    log.setElderId(elder.getId());
    log.setChangeType("DEDUCT");
    log.setChangePoints(pointsRequired);
    log.setBalanceAfter(balanceAfter);
    log.setRefOrderId(order.getId());
    log.setRemark("Store order deduct");
    pointsLogMapper.insert(log);

    deductInventoryFIFO(elder.getOrgId(), product.getId(), request.getQty(), order.getId());

    response.setStatus(OrderStatus.ALLOWED.name());
    response.setAllowed(true);
    response.setOrderId(order.getId());
    response.setOrderNo(orderNo);
    response.setPointsDeducted(pointsRequired);
    response.setBalanceAfter(balanceAfter);
    response.setMessage("Order created");
    return response;
  }

  @Override
  @Transactional
  public void cancel(OrderCancelRequest request) {
    StoreOrder order = orderMapper.selectById(request.getOrderId());
    if (order == null) {
      return;
    }
    if (order.getOrderStatus() != null && order.getOrderStatus() == 4) {
      return;
    }
    List<OrderItem> items = orderItemMapper.selectList(
        Wrappers.lambdaQuery(OrderItem.class).eq(OrderItem::getOrderId, order.getId()));
    boolean fulfilled = order.getOrderStatus() != null && order.getOrderStatus() >= 3;
    if (!fulfilled) {
      rollbackPoints(order);
      rollbackInventory(items, order);
    }
    order.setOrderStatus(4); // CANCELLED
    orderMapper.updateById(order);
  }

  @Override
  @Transactional
  public void refund(OrderRefundRequest request) {
    StoreOrder order = orderMapper.selectById(request.getOrderId());
    if (order == null) {
      return;
    }
    if (order.getOrderStatus() != null && order.getOrderStatus() == 5) {
      return;
    }
    List<OrderItem> items = orderItemMapper.selectList(
        Wrappers.lambdaQuery(OrderItem.class).eq(OrderItem::getOrderId, order.getId()));
    for (OrderItem item : items) {
      InventoryBatch batch = new InventoryBatch();
      batch.setOrgId(order.getOrgId());
      batch.setProductId(item.getProductId());
      batch.setBatchNo("RETURN-" + order.getOrderNo());
      batch.setQuantity(item.getQuantity());
      batch.setCostPrice(item.getUnitPrice());
      inventoryBatchMapper.insert(batch);

      InventoryLog log = new InventoryLog();
      log.setOrgId(order.getOrgId());
      log.setProductId(item.getProductId());
      log.setBatchId(batch.getId());
      log.setChangeType("IN");
      log.setChangeQty(item.getQuantity());
      log.setRefOrderId(order.getId());
      log.setRemark("Return in");
      inventoryLogMapper.insert(log);
    }
    rollbackPoints(order);
    order.setOrderStatus(5); // REFUNDED
    orderMapper.updateById(order);
  }

  private Set<Long> parseTagIds(String tagIds) {
    if (tagIds == null || tagIds.trim().isEmpty()) {
      return new HashSet<>();
    }
    return Arrays.stream(tagIds.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(Long::valueOf)
        .collect(Collectors.toSet());
  }

  private String generateOrderNo() {
    String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    int rand = new Random().nextInt(9000) + 1000;
    return "ORD" + time + rand;
  }

  private boolean hasSufficientInventory(Long orgId, Long productId, int qty) {
    List<InventoryBatch> batches = inventoryBatchMapper.selectList(
        Wrappers.lambdaQuery(InventoryBatch.class)
            .eq(InventoryBatch::getOrgId, orgId)
            .eq(InventoryBatch::getProductId, productId)
            .orderByAsc(InventoryBatch::getExpireDate)
            .orderByAsc(InventoryBatch::getId));
    int total = 0;
    for (InventoryBatch batch : batches) {
      if (batch.getQuantity() != null && batch.getQuantity() > 0) {
        total += batch.getQuantity();
        if (total >= qty) {
          return true;
        }
      }
    }
    return false;
  }

  private void deductInventoryFIFO(Long orgId, Long productId, int qty, Long orderId) {
    List<InventoryBatch> batches = inventoryBatchMapper.selectList(
        Wrappers.lambdaQuery(InventoryBatch.class)
            .eq(InventoryBatch::getOrgId, orgId)
            .eq(InventoryBatch::getProductId, productId)
            .orderByAsc(InventoryBatch::getExpireDate)
            .orderByAsc(InventoryBatch::getId));
    int remaining = qty;
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
      inventoryBatchMapper.updateById(batch);

      InventoryLog inventoryLog = new InventoryLog();
      inventoryLog.setOrgId(orgId);
      inventoryLog.setProductId(productId);
      inventoryLog.setBatchId(batch.getId());
      inventoryLog.setChangeType("OUT");
      inventoryLog.setChangeQty(used);
      inventoryLog.setRefOrderId(orderId);
      inventoryLog.setRemark("Store order out");
      inventoryLogMapper.insert(inventoryLog);

      remaining -= used;
    }
  }

  private void rollbackPoints(StoreOrder order) {
    ElderPointsAccount account = pointsAccountMapper.selectOne(
        Wrappers.lambdaQuery(ElderPointsAccount.class)
            .eq(ElderPointsAccount::getOrgId, order.getOrgId())
            .eq(ElderPointsAccount::getElderId, order.getElderId()));
    if (account == null || order.getPointsUsed() == null || order.getPointsUsed() <= 0) {
      return;
    }
    int balanceAfter = account.getPointsBalance() + order.getPointsUsed();
    account.setPointsBalance(balanceAfter);
    pointsAccountMapper.updateById(account);

    PointsLog log = new PointsLog();
    log.setOrgId(order.getOrgId());
    log.setElderId(order.getElderId());
    log.setChangeType("RECHARGE");
    log.setChangePoints(order.getPointsUsed());
    log.setBalanceAfter(balanceAfter);
    log.setRefOrderId(order.getId());
    log.setRemark("Order rollback");
    pointsLogMapper.insert(log);
  }

  private void rollbackInventory(List<OrderItem> items, StoreOrder order) {
    for (OrderItem item : items) {
      InventoryBatch batch = new InventoryBatch();
      batch.setOrgId(order.getOrgId());
      batch.setProductId(item.getProductId());
      batch.setBatchNo("CANCEL-" + order.getOrderNo());
      batch.setQuantity(item.getQuantity());
      batch.setCostPrice(item.getUnitPrice());
      inventoryBatchMapper.insert(batch);

      InventoryLog log = new InventoryLog();
      log.setOrgId(order.getOrgId());
      log.setProductId(item.getProductId());
      log.setBatchId(batch.getId());
      log.setChangeType("IN");
      log.setChangeQty(item.getQuantity());
      log.setRefOrderId(order.getId());
      log.setRemark("Cancel rollback");
      inventoryLogMapper.insert(log);
    }
  }
}
