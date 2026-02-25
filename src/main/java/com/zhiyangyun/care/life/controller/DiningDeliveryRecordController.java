package com.zhiyangyun.care.life.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.life.entity.DiningDeliveryRecord;
import com.zhiyangyun.care.life.entity.DiningMealOrder;
import com.zhiyangyun.care.life.mapper.DiningDeliveryRecordMapper;
import com.zhiyangyun.care.life.mapper.DiningMealOrderMapper;
import com.zhiyangyun.care.life.model.DiningDeliveryRecordRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
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

  public DiningDeliveryRecordController(
      DiningDeliveryRecordMapper deliveryRecordMapper, DiningMealOrderMapper mealOrderMapper) {
    this.deliveryRecordMapper = deliveryRecordMapper;
    this.mealOrderMapper = mealOrderMapper;
  }

  @GetMapping("/page")
  public Result<IPage<DiningDeliveryRecord>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) LocalDate dateFrom,
      @RequestParam(required = false) LocalDate dateTo,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword) {
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
    fillRecordFields(record, request, true);
    record.setCreatedBy(AuthContext.getStaffId());
    deliveryRecordMapper.insert(record);
    syncOrderStatus(record);
    return Result.ok(record);
  }

  @PutMapping("/{id}")
  public Result<DiningDeliveryRecord> update(@PathVariable Long id, @Valid @RequestBody DiningDeliveryRecordRequest request) {
    DiningDeliveryRecord record = deliveryRecordMapper.selectById(id);
    if (record == null || record.getIsDeleted() != null && record.getIsDeleted() == 1) {
      return Result.ok(null);
    }
    fillRecordFields(record, request, false);
    deliveryRecordMapper.updateById(record);
    syncOrderStatus(record);
    return Result.ok(record);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    DiningDeliveryRecord record = deliveryRecordMapper.selectById(id);
    if (record != null) {
      record.setIsDeleted(1);
      deliveryRecordMapper.updateById(record);
    }
    return Result.ok(null);
  }

  private void fillRecordFields(DiningDeliveryRecord record, DiningDeliveryRecordRequest request, boolean create) {
    record.setMealOrderId(request.getMealOrderId());
    record.setOrderNo(request.getOrderNo());
    record.setDeliveryAreaId(request.getDeliveryAreaId());
    record.setDeliveryAreaName(request.getDeliveryAreaName());
    record.setDeliveredBy(request.getDeliveredBy());
    record.setDeliveredByName(request.getDeliveredByName());
    record.setDeliveredAt(request.getDeliveredAt());
    if (create) {
      record.setStatus(request.getStatus() == null ? "PENDING" : request.getStatus());
    } else {
      record.setStatus(request.getStatus() == null ? record.getStatus() : request.getStatus());
    }
    record.setRemark(request.getRemark());
  }

  private void syncOrderStatus(DiningDeliveryRecord record) {
    DiningMealOrder order = mealOrderMapper.selectById(record.getMealOrderId());
    if (order == null || order.getIsDeleted() != null && order.getIsDeleted() == 1) {
      return;
    }
    if ("DELIVERED".equals(record.getStatus())) {
      order.setStatus("DELIVERED");
      mealOrderMapper.updateById(order);
    } else if ("FAILED".equals(record.getStatus()) && "DELIVERED".equals(order.getStatus())) {
      order.setStatus("DELIVERING");
      mealOrderMapper.updateById(order);
    }
  }
}
