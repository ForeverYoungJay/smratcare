package com.zhiyangyun.care.life.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.life.entity.DiningMealOrder;
import com.zhiyangyun.care.life.entity.DiningRiskInterceptLog;
import com.zhiyangyun.care.life.entity.DiningRiskOverride;
import com.zhiyangyun.care.life.mapper.DiningMealOrderMapper;
import com.zhiyangyun.care.life.mapper.DiningRiskInterceptLogMapper;
import com.zhiyangyun.care.life.mapper.DiningRiskOverrideMapper;
import com.zhiyangyun.care.life.model.DiningMealOrderRequest;
import com.zhiyangyun.care.life.model.DiningRiskCheckResponse;
import com.zhiyangyun.care.life.service.DiningRiskService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
@RequestMapping("/api/life/dining/order")
public class DiningMealOrderController {
  private static final Map<String, Set<String>> STATUS_TRANSITIONS = Map.of(
      "CREATED", Set.of("PREPARING", "CANCELLED"),
      "PREPARING", Set.of("DELIVERING", "CANCELLED"),
      "DELIVERING", Set.of("DELIVERED", "CANCELLED"),
      "DELIVERED", Set.of(),
      "CANCELLED", Set.of());

  private final DiningMealOrderMapper mealOrderMapper;
  private final DiningRiskService diningRiskService;
  private final DiningRiskInterceptLogMapper riskInterceptLogMapper;
  private final DiningRiskOverrideMapper riskOverrideMapper;
  private final AuditLogService auditLogService;
  private final ObjectMapper objectMapper;

  public DiningMealOrderController(
      DiningMealOrderMapper mealOrderMapper,
      DiningRiskService diningRiskService,
      DiningRiskInterceptLogMapper riskInterceptLogMapper,
      DiningRiskOverrideMapper riskOverrideMapper,
      AuditLogService auditLogService,
      ObjectMapper objectMapper) {
    this.mealOrderMapper = mealOrderMapper;
    this.diningRiskService = diningRiskService;
    this.riskInterceptLogMapper = riskInterceptLogMapper;
    this.riskOverrideMapper = riskOverrideMapper;
    this.auditLogService = auditLogService;
    this.objectMapper = objectMapper;
  }

  @GetMapping("/page")
  public Result<IPage<DiningMealOrder>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) LocalDate dateFrom,
      @RequestParam(required = false) LocalDate dateTo,
      @RequestParam(required = false) String mealType,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(DiningMealOrder.class)
        .eq(DiningMealOrder::getIsDeleted, 0)
        .eq(orgId != null, DiningMealOrder::getOrgId, orgId)
        .eq(mealType != null && !mealType.isBlank(), DiningMealOrder::getMealType, mealType)
        .eq(status != null && !status.isBlank(), DiningMealOrder::getStatus, status);
    if (dateFrom != null && dateTo != null) {
      wrapper.between(DiningMealOrder::getOrderDate, dateFrom, dateTo);
    } else if (dateFrom != null) {
      wrapper.ge(DiningMealOrder::getOrderDate, dateFrom);
    } else if (dateTo != null) {
      wrapper.le(DiningMealOrder::getOrderDate, dateTo);
    }
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(DiningMealOrder::getOrderNo, keyword)
          .or().like(DiningMealOrder::getElderName, keyword)
          .or().like(DiningMealOrder::getDishNames, keyword));
    }
    wrapper.orderByDesc(DiningMealOrder::getOrderDate).orderByDesc(DiningMealOrder::getCreateTime);
    return Result.ok(mealOrderMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping("/risk-check")
  public Result<DiningRiskCheckResponse> riskCheck(@Valid @RequestBody DiningMealOrderRequest request) {
    Long orgId = AuthContext.getOrgId();
    DiningRiskCheckResponse risk = diningRiskService.check(orgId, request.getElderId(), request.getElderName(), request.getDishNames());
    if (!risk.isAllowed()) {
      recordInterceptLog(orgId, risk, "RISK_CHECK");
    }
    return Result.ok(risk);
  }

  @PostMapping
  public Result<DiningMealOrder> create(@Valid @RequestBody DiningMealOrderRequest request) {
    Long orgId = AuthContext.getOrgId();
    DiningRiskCheckResponse risk = enforceRiskOrOverride(orgId, request, "ORDER_CREATE");

    DiningMealOrder order = new DiningMealOrder();
    order.setTenantId(orgId);
    order.setOrgId(orgId);
    order.setOrderNo(generateOrderNo());
    fillOrderFields(order, request, true);
    order.setElderId(risk.getElderId());
    order.setElderName(risk.getElderName());
    order.setCreatedBy(AuthContext.getStaffId());
    mealOrderMapper.insert(order);

    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "DINING_MEAL_ORDER", order.getId(), "创建点餐订单");
    return Result.ok(order);
  }

  @PutMapping("/{id}")
  public Result<DiningMealOrder> update(@PathVariable Long id, @Valid @RequestBody DiningMealOrderRequest request) {
    DiningMealOrder order = mealOrderMapper.selectById(id);
    if (order == null || order.getIsDeleted() != null && order.getIsDeleted() == 1) {
      return Result.ok(null);
    }

    Long orgId = AuthContext.getOrgId();
    DiningRiskCheckResponse risk = enforceRiskOrOverride(orgId, request, "ORDER_UPDATE");
    fillOrderFields(order, request, false);
    order.setElderId(risk.getElderId());
    order.setElderName(risk.getElderName());
    mealOrderMapper.updateById(order);

    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "DINING_MEAL_ORDER", order.getId(), "更新点餐订单");
    return Result.ok(order);
  }

  @PutMapping("/{id}/status")
  public Result<DiningMealOrder> updateStatus(@PathVariable Long id, @RequestParam String status) {
    DiningMealOrder order = mealOrderMapper.selectById(id);
    if (order == null || order.getIsDeleted() != null && order.getIsDeleted() == 1) {
      return Result.ok(null);
    }
    assertValidStatusTransition(order.getStatus(), status);
    order.setStatus(status);
    mealOrderMapper.updateById(order);
    Long orgId = AuthContext.getOrgId();
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE_STATUS", "DINING_MEAL_ORDER", order.getId(), "点餐状态变更为" + status);
    return Result.ok(order);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    DiningMealOrder order = mealOrderMapper.selectById(id);
    if (order != null) {
      order.setIsDeleted(1);
      mealOrderMapper.updateById(order);
    }
    return Result.ok(null);
  }

  private DiningRiskCheckResponse enforceRiskOrOverride(Long orgId, DiningMealOrderRequest request, String context) {
    DiningRiskCheckResponse risk = diningRiskService.check(orgId, request.getElderId(), request.getElderName(), request.getDishNames());
    if (risk.isAllowed()) {
      return risk;
    }

    if (request.getOverrideId() != null && isApprovedOverrideValid(orgId, request.getOverrideId(), risk)) {
      auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
          "OVERRIDE_USE", "DINING_RISK_OVERRIDE", request.getOverrideId(), "使用放行审批执行点餐");
      return risk;
    }

    recordInterceptLog(orgId, risk, context);
    throw new IllegalArgumentException("下单失败：检测到风险菜品[" + String.join("、", risk.getBlockedDishNames())
        + "]，" + risk.getMessage() + "；请先申请并通过放行审批");
  }

  private boolean isApprovedOverrideValid(Long orgId, Long overrideId, DiningRiskCheckResponse risk) {
    DiningRiskOverride override = riskOverrideMapper.selectOne(Wrappers.lambdaQuery(DiningRiskOverride.class)
        .eq(DiningRiskOverride::getIsDeleted, 0)
        .eq(orgId != null, DiningRiskOverride::getOrgId, orgId)
        .eq(DiningRiskOverride::getId, overrideId)
        .last("LIMIT 1"));
    if (override == null) {
      return false;
    }
    if (!"APPROVED".equals(override.getReviewStatus())) {
      return false;
    }
    if (override.getEffectiveUntil() != null && override.getEffectiveUntil().isBefore(LocalDateTime.now())) {
      return false;
    }
    if (!override.getElderId().equals(risk.getElderId())) {
      return false;
    }
    return override.getDishNames() != null && override.getDishNames().equals(risk.getDishNames());
  }

  private void recordInterceptLog(Long orgId, DiningRiskCheckResponse risk, String context) {
    DiningRiskInterceptLog log = new DiningRiskInterceptLog();
    log.setTenantId(orgId);
    log.setOrgId(orgId);
    log.setElderId(risk.getElderId());
    log.setElderName(risk.getElderName());
    log.setDishNames(risk.getDishNames());
    log.setOrderContext(context);
    log.setRiskDetail(toJson(risk));
    log.setCreatedBy(AuthContext.getStaffId());
    riskInterceptLogMapper.insert(log);
  }

  private void fillOrderFields(DiningMealOrder order, DiningMealOrderRequest request, boolean create) {
    order.setOrderDate(request.getOrderDate());
    order.setMealType(request.getMealType());
    order.setDishIds(request.getDishIds());
    order.setDishNames(request.getDishNames());
    order.setTotalAmount(request.getTotalAmount());
    order.setPrepZoneId(request.getPrepZoneId());
    order.setPrepZoneName(request.getPrepZoneName());
    order.setDeliveryAreaId(request.getDeliveryAreaId());
    order.setDeliveryAreaName(request.getDeliveryAreaName());
    if (create) {
      order.setStatus(request.getStatus() == null ? "CREATED" : request.getStatus());
    } else if (request.getStatus() != null && !request.getStatus().equals(order.getStatus())) {
      assertValidStatusTransition(order.getStatus(), request.getStatus());
      order.setStatus(request.getStatus());
    }
    order.setRemark(request.getRemark());
  }

  private void assertValidStatusTransition(String fromStatus, String toStatus) {
    if (toStatus == null || toStatus.isBlank()) {
      throw new IllegalArgumentException("状态不能为空");
    }
    if (fromStatus == null || fromStatus.isBlank() || fromStatus.equals(toStatus)) {
      return;
    }
    Set<String> allowed = STATUS_TRANSITIONS.get(fromStatus);
    if (allowed == null || !allowed.contains(toStatus)) {
      throw new IllegalArgumentException("非法状态流转：" + fromStatus + " -> " + toStatus);
    }
  }

  private String generateOrderNo() {
    return "MO" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
        + String.valueOf(System.currentTimeMillis() % 1000000L);
  }

  private String toJson(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      Map<String, String> fallback = new HashMap<>();
      fallback.put("message", "serialize_failed");
      return fallback.toString();
    }
  }
}
