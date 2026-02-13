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
import com.zhiyangyun.care.bill.entity.BillItem;
import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillItemMapper;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.finance.entity.PaymentRecord;
import com.zhiyangyun.care.finance.mapper.PaymentRecordMapper;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.oa.entity.OaTodo;
import com.zhiyangyun.care.oa.mapper.OaTodoMapper;
import com.zhiyangyun.care.store.model.ForbiddenReason;
import com.zhiyangyun.care.store.model.OrderCancelRequest;
import com.zhiyangyun.care.store.model.OrderFulfillRequest;
import com.zhiyangyun.care.store.model.OrderDetailResponse;
import com.zhiyangyun.care.store.model.OrderPreviewRequest;
import com.zhiyangyun.care.store.model.OrderPreviewResponse;
import com.zhiyangyun.care.store.model.OrderStatus;
import com.zhiyangyun.care.store.model.OrderRefundRequest;
import com.zhiyangyun.care.store.model.OrderSubmitResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
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
  private final BillMonthlyMapper billMonthlyMapper;
  private final BillItemMapper billItemMapper;
  private final PaymentRecordMapper paymentRecordMapper;
  private final StaffMapper staffMapper;
  private final OaTodoMapper oaTodoMapper;

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
      InventoryBatchMapper inventoryBatchMapper,
      BillMonthlyMapper billMonthlyMapper,
      BillItemMapper billItemMapper,
      PaymentRecordMapper paymentRecordMapper,
      StaffMapper staffMapper,
      OaTodoMapper oaTodoMapper) {
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
    this.billMonthlyMapper = billMonthlyMapper;
    this.billItemMapper = billItemMapper;
    this.paymentRecordMapper = paymentRecordMapper;
    this.staffMapper = staffMapper;
    this.oaTodoMapper = oaTodoMapper;
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
    int balanceAfter = account.getPointsBalance() - pointsRequired;

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
    order.setPayStatus(0);
    order.setOrderStatus(1); // 已创建
    orderMapper.insert(order);

    OrderItem item = new OrderItem();
    item.setOrgId(elder.getOrgId());
    item.setOrderId(order.getId());
    item.setProductId(product.getId());
    item.setProductName(product.getProductName());
    item.setProductCodeSnapshot(product.getProductCode());
    item.setProductNameSnapshot(product.getProductName());
    item.setUnitPrice(unitPrice);
    item.setQuantity(request.getQty());
    item.setTotalAmount(totalAmount);
    orderItemMapper.insert(item);

    // 积分扣减与库存扣减在标记出库时执行

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
    if (order.getOrderStatus() != null && order.getOrderStatus() >= 3) {
      return;
    }
    List<OrderItem> items = orderItemMapper.selectList(
        Wrappers.lambdaQuery(OrderItem.class).eq(OrderItem::getOrderId, order.getId()));
    // 未出库前不回滚库存、不回滚积分
    order.setOrderStatus(4); // CANCELLED
    order.setPayStatus(2);
    orderMapper.updateById(order);
    applyBillRefund(order, "CANCEL");
  }

  @Override
  @Transactional
  public void refund(OrderRefundRequest request) {
    StoreOrder order = orderMapper.selectById(request.getOrderId());
    if (order == null) {
      return;
    }
    if (order.getOrderStatus() == null || order.getOrderStatus() < 3) {
      return;
    }
    if (order.getOrderStatus() != null && order.getOrderStatus() == 5) {
      return;
    }
    List<OrderItem> items = orderItemMapper.selectList(
        Wrappers.lambdaQuery(OrderItem.class).eq(OrderItem::getOrderId, order.getId()));
    Map<Long, Integer> qtyByProduct = new HashMap<>();
    Map<Long, BigDecimal> priceByProduct = new HashMap<>();
    Map<Long, String> codeSnapshotByProduct = new HashMap<>();
    Map<Long, String> nameSnapshotByProduct = new HashMap<>();
    for (OrderItem item : items) {
      if (item.getProductId() == null) {
        continue;
      }
      qtyByProduct.merge(item.getProductId(), item.getQuantity(), Integer::sum);
      priceByProduct.putIfAbsent(item.getProductId(), item.getUnitPrice());
      if (item.getProductCodeSnapshot() != null) {
        codeSnapshotByProduct.putIfAbsent(item.getProductId(), item.getProductCodeSnapshot());
      }
      if (item.getProductNameSnapshot() != null) {
        nameSnapshotByProduct.putIfAbsent(item.getProductId(), item.getProductNameSnapshot());
      }
    }
    for (Map.Entry<Long, Integer> entry : qtyByProduct.entrySet()) {
      Long productId = entry.getKey();
      Integer qty = entry.getValue();
      if (qty == null || qty <= 0) {
        continue;
      }
      String batchNo = "RETURN-" + order.getOrderNo();
      InventoryBatch batch = inventoryBatchMapper.selectOne(
          Wrappers.lambdaQuery(InventoryBatch.class)
              .eq(InventoryBatch::getOrgId, order.getOrgId())
              .eq(InventoryBatch::getProductId, productId)
              .eq(InventoryBatch::getBatchNo, batchNo)
              .eq(InventoryBatch::getIsDeleted, 0));
      if (batch == null) {
        batch = new InventoryBatch();
        batch.setOrgId(order.getOrgId());
        batch.setProductId(productId);
        batch.setBatchNo(batchNo);
        batch.setQuantity(qty);
        batch.setCostPrice(priceByProduct.get(productId));
        inventoryBatchMapper.insert(batch);
      } else {
        batch.setQuantity(batch.getQuantity() + qty);
        inventoryBatchMapper.updateById(batch);
      }

      if (order.getOrderStatus() != null && order.getOrderStatus() >= 3) {
        InventoryLog log = new InventoryLog();
        log.setOrgId(order.getOrgId());
        log.setProductId(productId);
        log.setBatchId(batch.getId());
        log.setChangeType("IN");
        log.setChangeQty(qty);
        log.setRefOrderId(order.getId());
        log.setBizType("RETURN");
        log.setProductCodeSnapshot(codeSnapshotByProduct.get(productId));
        log.setProductNameSnapshot(nameSnapshotByProduct.get(productId));
        log.setRemark("Return in");
        inventoryLogMapper.insert(log);
      }
    }
    if (order.getPayStatus() != null && order.getPayStatus() == 1) {
      rollbackPoints(order);
    }
    order.setOrderStatus(5); // REFUNDED
    order.setPayStatus(2);
    orderMapper.updateById(order);
    applyBillRefund(order, "REFUND");
  }

  @Override
  @Transactional
  public void fulfill(OrderFulfillRequest request) {
    StoreOrder order = orderMapper.selectById(request.getOrderId());
    if (order == null) {
      return;
    }
    if (order.getOrderStatus() != null && order.getOrderStatus() >= 3) {
      return;
    }
    // 积分扣减（出库时）
    ElderPointsAccount account = pointsAccountMapper.selectOne(
        Wrappers.lambdaQuery(ElderPointsAccount.class)
            .eq(ElderPointsAccount::getOrgId, order.getOrgId())
            .eq(ElderPointsAccount::getElderId, order.getElderId()));
    int pointsRequired = order.getPointsUsed() == null ? 0 : order.getPointsUsed();
    if (pointsRequired > 0) {
      if (account == null || account.getPointsBalance() == null || account.getPointsBalance() < pointsRequired) {
        throw new IllegalStateException("Insufficient points");
      }
      int balanceAfter = account.getPointsBalance() - pointsRequired;
      account.setPointsBalance(balanceAfter);
      pointsAccountMapper.updateById(account);

      PointsLog log = new PointsLog();
      log.setOrgId(order.getOrgId());
      log.setElderId(order.getElderId());
      log.setChangeType("DEDUCT");
      log.setChangePoints(pointsRequired);
      log.setBalanceAfter(balanceAfter);
      log.setRefOrderId(order.getId());
      log.setRemark("Store order deduct");
      pointsLogMapper.insert(log);
    }
    List<OrderItem> items = orderItemMapper.selectList(
        Wrappers.lambdaQuery(OrderItem.class).eq(OrderItem::getOrderId, order.getId()));
    for (OrderItem item : items) {
      Product product = productMapper.selectById(item.getProductId());
      if (product == null) {
        continue;
      }
      deductInventoryFIFO(order.getOrgId(), product, item.getQuantity(), order.getId());
    }
    createFinanceFlow(order);
    createFulfillTodos(order);
    order.setOrderStatus(3); // FULFILLED
    order.setPayStatus(1);
    order.setPayTime(LocalDateTime.now());
    order.setUpdateTime(LocalDateTime.now());
    orderMapper.updateById(order);
  }

  @Override
  public OrderDetailResponse getOrderDetail(Long orderId) {
    StoreOrder order = orderMapper.selectById(orderId);
    if (order == null) {
      return null;
    }
    OrderDetailResponse resp = new OrderDetailResponse();
    resp.setId(order.getId());
    resp.setOrderNo(order.getOrderNo());
    resp.setElderId(order.getElderId());
    ElderProfile elder = elderMapper.selectById(order.getElderId());
    resp.setElderName(elder == null ? null : elder.getFullName());
    resp.setTotalAmount(order.getTotalAmount());
    resp.setPayableAmount(order.getPayableAmount());
    resp.setPointsUsed(order.getPointsUsed());
    resp.setOrderStatus(order.getOrderStatus());
    resp.setPayStatus(order.getPayStatus());
    resp.setPayTime(order.getPayTime());
    resp.setCreateTime(order.getCreateTime());

    List<OrderItem> items = orderItemMapper.selectList(
        Wrappers.lambdaQuery(OrderItem.class)
            .eq(OrderItem::getOrderId, order.getId())
            .eq(OrderItem::getIsDeleted, 0));
    List<Long> productIds = items.stream()
        .map(OrderItem::getProductId)
        .filter(java.util.Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, Product> productMap = productIds.isEmpty()
        ? Map.of()
        : productMapper.selectBatchIds(productIds).stream()
            .collect(Collectors.toMap(Product::getId, p -> p));
    List<OrderDetailResponse.OrderLineItem> lineItems = items.stream().map(item -> {
      OrderDetailResponse.OrderLineItem line = new OrderDetailResponse.OrderLineItem();
      line.setProductId(item.getProductId());
      line.setProductName(item.getProductName());
      line.setQuantity(item.getQuantity());
      line.setUnitPrice(item.getUnitPrice());
      line.setAmount(item.getTotalAmount());
      return line;
    }).toList();
    resp.setItems(lineItems);

    resp.setRiskReasons(buildRiskReasons(order.getOrgId(), order.getElderId(), productMap));

    List<InventoryLog> logs = inventoryLogMapper.selectList(
        Wrappers.lambdaQuery(InventoryLog.class)
            .eq(InventoryLog::getRefOrderId, order.getId())
            .eq(InventoryLog::getChangeType, "OUT")
            .eq(InventoryLog::getIsDeleted, 0));
    if (logs.isEmpty()) {
      resp.setFifoLogs(List.of());
      return resp;
    }
    List<Long> batchIds = logs.stream()
        .map(InventoryLog::getBatchId)
        .filter(java.util.Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, InventoryBatch> batchMap = batchIds.isEmpty()
        ? Map.of()
        : inventoryBatchMapper.selectBatchIds(batchIds)
            .stream()
            .collect(Collectors.toMap(InventoryBatch::getId, b -> b));
    List<OrderDetailResponse.FifoLogItem> fifoLogs = logs.stream().map(log -> {
      OrderDetailResponse.FifoLogItem item = new OrderDetailResponse.FifoLogItem();
      InventoryBatch batch = batchMap.get(log.getBatchId());
      item.setBatchNo(batch != null ? batch.getBatchNo() : null);
      item.setQuantity(log.getChangeQty());
      item.setExpireDate(batch != null && batch.getExpireDate() != null ? batch.getExpireDate().toString() : null);
      return item;
    }).toList();
    resp.setFifoLogs(fifoLogs);
    return resp;
  }

  private List<OrderDetailResponse.RiskReason> buildRiskReasons(Long orgId, Long elderId,
      Map<Long, Product> productMap) {
    if (orgId == null || elderId == null || productMap.isEmpty()) {
      return List.of();
    }
    Set<Long> productTagIds = new HashSet<>();
    for (Product product : productMap.values()) {
      productTagIds.addAll(parseTagIds(product.getTagIds()));
    }
    if (productTagIds.isEmpty()) {
      return List.of();
    }
    List<ElderDisease> elderDiseases = elderDiseaseMapper.selectList(
        Wrappers.lambdaQuery(ElderDisease.class)
            .eq(ElderDisease::getOrgId, orgId)
            .eq(ElderDisease::getElderId, elderId)
            .eq(ElderDisease::getIsDeleted, 0));
    if (elderDiseases.isEmpty()) {
      return List.of();
    }
    List<Long> diseaseIds = elderDiseases.stream()
        .map(ElderDisease::getDiseaseId)
        .filter(java.util.Objects::nonNull)
        .distinct()
        .toList();
    if (diseaseIds.isEmpty()) {
      return List.of();
    }
    Map<Long, Disease> diseaseMap = diseaseMapper.selectBatchIds(diseaseIds).stream()
        .collect(Collectors.toMap(Disease::getId, d -> d));
    List<DiseaseForbiddenTag> forbiddenTags = forbiddenTagMapper.selectList(
        Wrappers.lambdaQuery(DiseaseForbiddenTag.class)
            .eq(DiseaseForbiddenTag::getOrgId, orgId)
            .in(DiseaseForbiddenTag::getDiseaseId, diseaseIds)
            .eq(DiseaseForbiddenTag::getIsDeleted, 0));
    if (forbiddenTags.isEmpty()) {
      return List.of();
    }
    Set<Long> forbiddenTagIds = forbiddenTags.stream()
        .map(DiseaseForbiddenTag::getTagId)
        .filter(java.util.Objects::nonNull)
        .collect(Collectors.toSet());
    Set<Long> hitTagIds = productTagIds.stream()
        .filter(forbiddenTagIds::contains)
        .collect(Collectors.toSet());
    if (hitTagIds.isEmpty()) {
      return List.of();
    }
    Map<Long, ProductTag> tagMap = productTagMapper.selectBatchIds(hitTagIds).stream()
        .collect(Collectors.toMap(ProductTag::getId, t -> t));
    Set<String> dedup = new HashSet<>();
    List<OrderDetailResponse.RiskReason> reasons = new ArrayList<>();
    for (DiseaseForbiddenTag forbiddenTag : forbiddenTags) {
      if (!hitTagIds.contains(forbiddenTag.getTagId())) {
        continue;
      }
      String key = forbiddenTag.getDiseaseId() + "-" + forbiddenTag.getTagId();
      if (!dedup.add(key)) {
        continue;
      }
      Disease disease = diseaseMap.get(forbiddenTag.getDiseaseId());
      ProductTag tag = tagMap.get(forbiddenTag.getTagId());
      OrderDetailResponse.RiskReason reason = new OrderDetailResponse.RiskReason();
      reason.setDiseaseName(disease != null ? disease.getDiseaseName() : null);
      reason.setTagCode(tag != null ? tag.getTagCode() : null);
      reason.setTagName(tag != null ? tag.getTagName() : null);
      reasons.add(reason);
    }
    return reasons;
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

  private void deductInventoryFIFO(Long orgId, Product product, int qty, Long orderId) {
    List<InventoryBatch> batches = inventoryBatchMapper.selectList(
        Wrappers.lambdaQuery(InventoryBatch.class)
            .eq(InventoryBatch::getOrgId, orgId)
            .eq(InventoryBatch::getProductId, product.getId())
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
      inventoryLog.setProductId(product.getId());
      inventoryLog.setBatchId(batch.getId());
      inventoryLog.setChangeType("OUT");
      inventoryLog.setChangeQty(used);
      inventoryLog.setRefOrderId(orderId);
      inventoryLog.setBizType("ORDER");
      inventoryLog.setProductCodeSnapshot(product.getProductCode());
      inventoryLog.setProductNameSnapshot(product.getProductName());
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
      log.setBizType("RETURN");
      log.setProductCodeSnapshot(item.getProductCodeSnapshot());
      log.setProductNameSnapshot(item.getProductNameSnapshot());
      log.setRemark("Cancel rollback");
      inventoryLogMapper.insert(log);
    }
  }

  private void applyBillRefund(StoreOrder order, String type) {
    if (order.getPayTime() == null || order.getTotalAmount() == null) {
      return;
    }
    YearMonth ym = YearMonth.from(order.getPayTime());
    String billMonth = ym.toString();
    BillMonthly monthly = billMonthlyMapper.selectOne(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getOrgId, order.getOrgId())
            .eq(BillMonthly::getElderId, order.getElderId())
            .eq(BillMonthly::getBillMonth, billMonth));
    if (monthly == null) {
      return;
    }
    BillItem item = new BillItem();
    item.setOrgId(order.getOrgId());
    item.setBillMonthlyId(monthly.getId());
    item.setItemType("SHOP_" + type);
    item.setItemName("商城退款冲销");
    item.setAmount(order.getTotalAmount().negate());
    item.setRefOrderId(order.getId());
    item.setRemark(type);
    billItemMapper.insert(item);

    if (monthly.getTotalAmount() != null) {
      monthly.setTotalAmount(monthly.getTotalAmount().subtract(order.getTotalAmount()));
    }
    if (monthly.getOutstandingAmount() != null) {
      monthly.setOutstandingAmount(monthly.getOutstandingAmount().subtract(order.getTotalAmount()));
    }
    billMonthlyMapper.updateById(monthly);
  }

  private void createFinanceFlow(StoreOrder order) {
    if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
      return;
    }
    LocalDateTime paidAt = LocalDateTime.now();
    BillMonthly monthly = ensureBillMonthly(order.getOrgId(), order.getElderId(), paidAt);
    if (monthly == null) {
      return;
    }
    PaymentRecord record = new PaymentRecord();
    record.setOrgId(order.getOrgId());
    record.setBillMonthlyId(monthly.getId());
    record.setAmount(order.getTotalAmount());
    record.setPayMethod("POINTS");
    record.setPaidAt(paidAt);
    record.setRemark("商城订单出库自动收款: " + order.getOrderNo());
    paymentRecordMapper.insert(record);

    if (monthly.getPaidAmount() == null) {
      monthly.setPaidAmount(BigDecimal.ZERO);
    }
    monthly.setPaidAmount(monthly.getPaidAmount().add(order.getTotalAmount()));
    if (monthly.getTotalAmount() != null && monthly.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
      BigDecimal outstanding = monthly.getTotalAmount().subtract(monthly.getPaidAmount());
      monthly.setOutstandingAmount(outstanding);
    }
    billMonthlyMapper.updateById(monthly);
  }

  private BillMonthly ensureBillMonthly(Long orgId, Long elderId, LocalDateTime paidAt) {
    if (orgId == null || elderId == null) {
      return null;
    }
    String billMonth = YearMonth.from(paidAt).toString();
    BillMonthly monthly = billMonthlyMapper.selectOne(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getOrgId, orgId)
            .eq(BillMonthly::getElderId, elderId)
            .eq(BillMonthly::getBillMonth, billMonth));
    if (monthly != null) {
      return monthly;
    }
    monthly = new BillMonthly();
    monthly.setOrgId(orgId);
    monthly.setElderId(elderId);
    monthly.setBillMonth(billMonth);
    monthly.setTotalAmount(BigDecimal.ZERO);
    monthly.setPaidAmount(BigDecimal.ZERO);
    monthly.setOutstandingAmount(BigDecimal.ZERO);
    monthly.setStatus(0);
    billMonthlyMapper.insert(monthly);
    return monthly;
  }

  private void createFulfillTodos(StoreOrder order) {
    String title = "商城订单出库待处理";
    String content = "订单号: " + order.getOrderNo() + "，金额: " + order.getTotalAmount()
        + "，积分: " + (order.getPointsUsed() == null ? 0 : order.getPointsUsed());
    createTodoForRole(order.getOrgId(), "CARE_WORKER", title, content);
    createTodoForRole(order.getOrgId(), "ADMIN", title, content);
  }

  private void createTodoForRole(Long orgId, String roleCode, String title, String content) {
    if (orgId == null) {
      return;
    }
    List<StaffAccount> staffs = staffMapper.selectByRoleCode(orgId, roleCode);
    if (staffs == null || staffs.isEmpty()) {
      return;
    }
    for (StaffAccount staff : staffs) {
      OaTodo todo = new OaTodo();
      todo.setTenantId(orgId);
      todo.setOrgId(orgId);
      todo.setTitle(title);
      todo.setContent(content);
      todo.setStatus("OPEN");
      todo.setAssigneeId(staff.getId());
      todo.setAssigneeName(staff.getRealName());
      oaTodoMapper.insert(todo);
    }
  }
}
