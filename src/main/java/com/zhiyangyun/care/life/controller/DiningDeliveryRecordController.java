package com.zhiyangyun.care.life.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.life.entity.DiningDeliveryArea;
import com.zhiyangyun.care.life.entity.DiningDeliveryRecord;
import com.zhiyangyun.care.life.entity.DiningMealOrder;
import com.zhiyangyun.care.life.mapper.DiningDeliveryAreaMapper;
import com.zhiyangyun.care.life.mapper.DiningDeliveryRecordMapper;
import com.zhiyangyun.care.life.mapper.DiningMealOrderMapper;
import com.zhiyangyun.care.life.model.DiningConstants;
import com.zhiyangyun.care.life.model.DiningDeliveryRecordRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
@RequestMapping("/api/life/dining/delivery-record")
public class DiningDeliveryRecordController {
  private final DiningDeliveryRecordMapper deliveryRecordMapper;
  private final DiningMealOrderMapper mealOrderMapper;
  private final DiningDeliveryAreaMapper deliveryAreaMapper;

  public DiningDeliveryRecordController(
      DiningDeliveryRecordMapper deliveryRecordMapper,
      DiningMealOrderMapper mealOrderMapper,
      DiningDeliveryAreaMapper deliveryAreaMapper) {
    this.deliveryRecordMapper = deliveryRecordMapper;
    this.mealOrderMapper = mealOrderMapper;
    this.deliveryAreaMapper = deliveryAreaMapper;
  }

  @GetMapping("/page")
  public Result<IPage<DiningDeliveryRecord>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) LocalDate dateFrom,
      @RequestParam(required = false) LocalDate dateTo,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword) {
    if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
      throw new IllegalArgumentException(DiningConstants.MSG_INVALID_DATE_RANGE);
    }
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(DiningDeliveryRecord.class)
        .eq(DiningDeliveryRecord::getIsDeleted, 0)
        .eq(orgId != null, DiningDeliveryRecord::getOrgId, orgId)
        .eq(status != null && !status.isBlank(), DiningDeliveryRecord::getStatus, status);
    if (dateFrom != null && dateTo != null) {
      wrapper.between(DiningDeliveryRecord::getDeliveredAt,
          dateFrom.atStartOfDay(), dateTo.plusDays(1).atStartOfDay());
    } else if (dateFrom != null) {
      wrapper.ge(DiningDeliveryRecord::getDeliveredAt, dateFrom.atStartOfDay());
    } else if (dateTo != null) {
      wrapper.le(DiningDeliveryRecord::getDeliveredAt, dateTo.plusDays(1).atStartOfDay());
    }
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(DiningDeliveryRecord::getOrderNo, keyword)
          .or().like(DiningDeliveryRecord::getDeliveryAreaName, keyword)
          .or().like(DiningDeliveryRecord::getDeliveredByName, keyword));
    }
    wrapper.orderByDesc(DiningDeliveryRecord::getDeliveredAt).orderByDesc(DiningDeliveryRecord::getCreateTime);
    return Result.ok(deliveryRecordMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping
  public Result<DiningDeliveryRecord> create(@Valid @RequestBody DiningDeliveryRecordRequest request) {
    Long orgId = AuthContext.getOrgId();
    DiningDeliveryRecord record = new DiningDeliveryRecord();
    record.setTenantId(orgId);
    record.setOrgId(orgId);
    fillRecordFields(record, orgId, request, true);
    record.setCreatedBy(AuthContext.getStaffId());
    deliveryRecordMapper.insert(record);
    syncOrderStatus(record);
    return Result.ok(record);
  }

  @PutMapping("/{id}")
  public Result<DiningDeliveryRecord> update(@PathVariable Long id, @Valid @RequestBody DiningDeliveryRecordRequest request) {
    Long orgId = AuthContext.getOrgId();
    DiningDeliveryRecord record = getRecordInOrg(id, orgId);
    if (record == null) {
      return Result.ok(null);
    }
    fillRecordFields(record, orgId, request, false);
    deliveryRecordMapper.updateById(record);
    syncOrderStatus(record);
    return Result.ok(record);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    DiningDeliveryRecord record = getRecordInOrg(id, AuthContext.getOrgId());
    if (record != null) {
      record.setIsDeleted(1);
      deliveryRecordMapper.updateById(record);
    }
    return Result.ok(null);
  }

  private void fillRecordFields(DiningDeliveryRecord record, Long orgId, DiningDeliveryRecordRequest request, boolean create) {
    DiningMealOrder order = resolveMealOrder(orgId, request.getMealOrderId());
    record.setMealOrderId(order.getId());
    record.setOrderNo(order.getOrderNo());
    applyDeliveryArea(record, orgId, request.getDeliveryAreaId(), request.getDeliveryAreaName());
    record.setDeliveredBy(request.getDeliveredBy());
    record.setDeliveredByName(normalizeText(request.getDeliveredByName()));
    String status = request.getStatus() == null
        ? (create ? DiningConstants.DELIVERY_STATUS_PENDING : record.getStatus())
        : request.getStatus();
    validateStatus(status);
    record.setStatus(status);
    record.setDeliveredAt(resolveDeliveredAt(request.getDeliveredAt(), status, record.getDeliveredAt()));
    if (create) {
      if (record.getDeliveryAreaId() == null && order.getDeliveryAreaId() != null) {
        record.setDeliveryAreaId(order.getDeliveryAreaId());
        record.setDeliveryAreaName(order.getDeliveryAreaName());
      }
    }
    record.setRemark(normalizeText(request.getRemark()));
  }

  private void syncOrderStatus(DiningDeliveryRecord record) {
    DiningMealOrder order = resolveMealOrder(AuthContext.getOrgId(), record.getMealOrderId());
    if (order == null) {
      return;
    }
    if (DiningConstants.DELIVERY_STATUS_DELIVERED.equals(record.getStatus())) {
      order.setStatus(DiningConstants.ORDER_STATUS_DELIVERED);
      mealOrderMapper.updateById(order);
    } else if (DiningConstants.DELIVERY_STATUS_FAILED.equals(record.getStatus())
        && DiningConstants.ORDER_STATUS_DELIVERED.equals(order.getStatus())) {
      order.setStatus(DiningConstants.ORDER_STATUS_DELIVERING);
      mealOrderMapper.updateById(order);
    }
  }

  private DiningDeliveryRecord getRecordInOrg(Long id, Long orgId) {
    return deliveryRecordMapper.selectOne(Wrappers.lambdaQuery(DiningDeliveryRecord.class)
        .eq(DiningDeliveryRecord::getId, id)
        .eq(DiningDeliveryRecord::getIsDeleted, 0)
        .eq(orgId != null, DiningDeliveryRecord::getOrgId, orgId)
        .last("LIMIT 1"));
  }

  private DiningMealOrder resolveMealOrder(Long orgId, Long mealOrderId) {
    DiningMealOrder order = mealOrderMapper.selectOne(Wrappers.lambdaQuery(DiningMealOrder.class)
        .eq(DiningMealOrder::getId, mealOrderId)
        .eq(DiningMealOrder::getIsDeleted, 0)
        .eq(orgId != null, DiningMealOrder::getOrgId, orgId)
        .last("LIMIT 1"));
    if (order == null) {
      throw new IllegalArgumentException(DiningConstants.MSG_ORDER_NOT_FOUND_OR_FORBIDDEN);
    }
    return order;
  }

  private void applyDeliveryArea(DiningDeliveryRecord record, Long orgId, Long areaId, String areaName) {
    if (areaId == null) {
      record.setDeliveryAreaId(null);
      record.setDeliveryAreaName(normalizeText(areaName));
      return;
    }
    DiningDeliveryArea area = deliveryAreaMapper.selectOne(Wrappers.lambdaQuery(DiningDeliveryArea.class)
        .eq(DiningDeliveryArea::getId, areaId)
        .eq(DiningDeliveryArea::getIsDeleted, 0)
        .eq(orgId != null, DiningDeliveryArea::getOrgId, orgId)
        .last("LIMIT 1"));
    if (area == null) {
      throw new IllegalArgumentException(DiningConstants.MSG_DELIVERY_AREA_NOT_FOUND_OR_FORBIDDEN);
    }
    record.setDeliveryAreaId(area.getId());
    record.setDeliveryAreaName(area.getAreaName());
  }

  private LocalDateTime resolveDeliveredAt(LocalDateTime requestValue, String status, LocalDateTime currentValue) {
    if (requestValue != null) {
      return requestValue;
    }
    if (DiningConstants.DELIVERY_STATUS_DELIVERED.equals(status)
        || DiningConstants.DELIVERY_STATUS_FAILED.equals(status)) {
      return LocalDateTime.now();
    }
    return currentValue;
  }

  private void validateStatus(String status) {
    if (status == null || !DiningConstants.DELIVERY_STATUS_SET.contains(status)) {
      throw new IllegalArgumentException(DiningConstants.MSG_INVALID_DELIVERY_STATUS);
    }
  }

  private String normalizeText(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
