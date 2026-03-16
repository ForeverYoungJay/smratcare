package com.zhiyangyun.care.life.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.life.entity.DiningDeliveryArea;
import com.zhiyangyun.care.life.entity.DiningDeliveryRecord;
import com.zhiyangyun.care.life.entity.DiningMealOrder;
import com.zhiyangyun.care.life.mapper.DiningDeliveryAreaMapper;
import com.zhiyangyun.care.life.mapper.DiningDeliveryRecordMapper;
import com.zhiyangyun.care.life.mapper.DiningMealOrderMapper;
import com.zhiyangyun.care.life.model.DiningConstants;
import com.zhiyangyun.care.life.model.DiningDeliveryRedispatchRequest;
import com.zhiyangyun.care.life.model.DiningDeliveryRecordRequest;
import com.zhiyangyun.care.life.model.DiningDeliveryScanSignoffRequest;
import com.zhiyangyun.care.life.model.DiningDeliverySignoffQrResponse;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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
  private final ElderMapper elderMapper;
  private final BedMapper bedMapper;

  public DiningDeliveryRecordController(
      DiningDeliveryRecordMapper deliveryRecordMapper,
      DiningMealOrderMapper mealOrderMapper,
      DiningDeliveryAreaMapper deliveryAreaMapper,
      ElderMapper elderMapper,
      BedMapper bedMapper) {
    this.deliveryRecordMapper = deliveryRecordMapper;
    this.mealOrderMapper = mealOrderMapper;
    this.deliveryAreaMapper = deliveryAreaMapper;
    this.elderMapper = elderMapper;
    this.bedMapper = bedMapper;
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
      LocalDateTime start = dateFrom.atStartOfDay();
      LocalDateTime endExclusive = dateTo.plusDays(1).atStartOfDay();
      wrapper.and(w -> w
          .between(DiningDeliveryRecord::getDeliveredAt, start, endExclusive)
          .or(inner -> inner.isNull(DiningDeliveryRecord::getDeliveredAt)
              .between(DiningDeliveryRecord::getCreateTime, start, endExclusive)));
    } else if (dateFrom != null) {
      LocalDateTime start = dateFrom.atStartOfDay();
      wrapper.and(w -> w
          .ge(DiningDeliveryRecord::getDeliveredAt, start)
          .or(inner -> inner.isNull(DiningDeliveryRecord::getDeliveredAt)
              .ge(DiningDeliveryRecord::getCreateTime, start)));
    } else if (dateTo != null) {
      LocalDateTime endExclusive = dateTo.plusDays(1).atStartOfDay();
      wrapper.and(w -> w
          .lt(DiningDeliveryRecord::getDeliveredAt, endExclusive)
          .or(inner -> inner.isNull(DiningDeliveryRecord::getDeliveredAt)
              .lt(DiningDeliveryRecord::getCreateTime, endExclusive)));
    }
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(DiningDeliveryRecord::getOrderNo, keyword)
          .or().like(DiningDeliveryRecord::getDeliveryAreaName, keyword)
          .or().like(DiningDeliveryRecord::getDeliveredByName, keyword));
    }
    wrapper.orderByDesc(DiningDeliveryRecord::getDeliveredAt).orderByDesc(DiningDeliveryRecord::getCreateTime);
    IPage<DiningDeliveryRecord> page = deliveryRecordMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    hydrateRecords(page.getRecords());
    return Result.ok(page);
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
    hydrateRecord(record);
    syncOrderStatus(record);
    return Result.ok(record);
  }

  @PostMapping("/{id}/qr/generate")
  public Result<DiningDeliverySignoffQrResponse> generateSignoffQr(@PathVariable Long id) {
    DiningDeliveryRecord record = getRecordInOrg(id, AuthContext.getOrgId());
    if (record == null) {
      return Result.ok(null);
    }
    DiningMealOrder order = resolveMealOrder(AuthContext.getOrgId(), record.getMealOrderId());
    String qrCode = resolveResidentQrCode(order);
    if (qrCode == null || qrCode.isBlank()) {
      throw new IllegalArgumentException("当前订单关联长者未配置床位二维码或老人二维码");
    }
    LocalDateTime now = LocalDateTime.now();

    DiningDeliverySignoffQrResponse response = new DiningDeliverySignoffQrResponse();
    response.setRecordId(record.getId());
    response.setQrToken(qrCode);
    response.setQrContent(qrCode);
    response.setGeneratedAt(now);
    return Result.ok(response);
  }

  @PostMapping("/scan/signoff")
  public Result<DiningDeliveryRecord> scanSignoff(@Valid @RequestBody DiningDeliveryScanSignoffRequest request) {
    Long orgId = AuthContext.getOrgId();
    String token = request.getQrToken() == null ? null : request.getQrToken().trim();
    DiningDeliveryRecord record = resolveCurrentRecordByResidentQr(orgId, token);
    if (record == null) {
      return Result.error(404, "未匹配到送餐签收记录，请核对二维码");
    }
    record.setStatus(DiningConstants.DELIVERY_STATUS_DELIVERED);
    record.setQrScanAt(request.getQrScanAt() == null ? LocalDateTime.now() : request.getQrScanAt());
    record.setSignedAt(request.getSignedAt() == null ? record.getQrScanAt() : request.getSignedAt());
    if (request.getDeliveredByName() != null && !request.getDeliveredByName().isBlank()) {
      record.setDeliveredByName(normalizeText(request.getDeliveredByName()));
    }
    if (record.getDeliveredAt() == null) {
      record.setDeliveredAt(record.getSignedAt());
    }
    record.setSignoffImageUrlsText(joinImageUrls(request.getSignoffImageUrls()));
    if (request.getRemark() != null && !request.getRemark().isBlank()) {
      record.setRemark(normalizeText(request.getRemark()));
    }
    deliveryRecordMapper.updateById(record);
    hydrateRecord(record);
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
    hydrateRecord(record);
    syncOrderStatus(record);
    return Result.ok(record);
  }

  @PutMapping("/{id}/redispatch")
  public Result<DiningDeliveryRecord> redispatch(
      @PathVariable Long id,
      @RequestBody DiningDeliveryRedispatchRequest request) {
    Long orgId = AuthContext.getOrgId();
    DiningDeliveryRecord record = getRecordInOrg(id, orgId);
    if (record == null) {
      return Result.ok(null);
    }
    record.setStatus(DiningConstants.DELIVERY_STATUS_PENDING);
    record.setDeliveredAt(null);
    record.setSignedAt(null);
    record.setQrScanAt(null);
    record.setSignoffImageUrlsText(null);
    record.setRedispatchStatus("REDISPATCHED");
    record.setRedispatchAt(request == null ? LocalDateTime.now() : request.getRedispatchAt());
    record.setRedispatchByName(request == null ? null : normalizeText(request.getRedispatchByName()));
    record.setRedispatchRemark(request == null ? null : normalizeText(request.getRedispatchRemark()));
    deliveryRecordMapper.updateById(record);
    hydrateRecord(record);
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
    DiningMealOrder order = resolveMealOrderForUpsert(orgId, request.getMealOrderId(), record.getMealOrderId(), create);
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
    record.setFailureReason(normalizeText(request.getFailureReason()));
    record.setRedispatchStatus(resolveRedispatchStatus(request.getRedispatchStatus(), record.getRedispatchStatus(), create));
    record.setRedispatchAt(request.getRedispatchAt() == null ? record.getRedispatchAt() : request.getRedispatchAt());
    record.setRedispatchByName(normalizeText(request.getRedispatchByName()) == null
        ? record.getRedispatchByName()
        : normalizeText(request.getRedispatchByName()));
    record.setRedispatchRemark(normalizeText(request.getRedispatchRemark()) == null
        ? record.getRedispatchRemark()
        : normalizeText(request.getRedispatchRemark()));
    record.setDeliveredAt(resolveDeliveredAt(request.getDeliveredAt(), status, record.getDeliveredAt()));
    record.setSignedAt(resolveSignedAt(request.getSignedAt(), request.getQrScanAt(), status, record.getSignedAt()));
    record.setQrScanAt(request.getQrScanAt() == null ? record.getQrScanAt() : request.getQrScanAt());
    record.setSignoffImageUrlsText(joinImageUrls(request.getSignoffImageUrls()));
    if (create) {
      if (record.getDeliveryAreaId() == null && order.getDeliveryAreaId() != null) {
        record.setDeliveryAreaId(order.getDeliveryAreaId());
        record.setDeliveryAreaName(order.getDeliveryAreaName());
      }
    }
    record.setRemark(normalizeText(request.getRemark()));
  }

  private DiningMealOrder resolveMealOrderForUpsert(
      Long orgId,
      Long requestMealOrderId,
      Long currentMealOrderId,
      boolean create) {
    Long targetMealOrderId = requestMealOrderId;
    if (targetMealOrderId == null && !create) {
      targetMealOrderId = currentMealOrderId;
    }
    if (targetMealOrderId == null) {
      throw new IllegalArgumentException(DiningConstants.MSG_ORDER_NOT_FOUND_OR_FORBIDDEN);
    }
    return resolveMealOrder(orgId, targetMealOrderId);
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
    } else if (DiningConstants.DELIVERY_STATUS_PENDING.equals(record.getStatus())
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

  private DiningDeliveryRecord resolveCurrentRecordByResidentQr(Long orgId, String qrCode) {
    if (qrCode == null || qrCode.isBlank()) {
      throw new IllegalArgumentException("qrToken 不能为空");
    }
    List<DiningDeliveryRecord> candidates = deliveryRecordMapper.selectList(Wrappers.lambdaQuery(DiningDeliveryRecord.class)
        .eq(DiningDeliveryRecord::getIsDeleted, 0)
        .eq(orgId != null, DiningDeliveryRecord::getOrgId, orgId)
        .in(DiningDeliveryRecord::getStatus, DiningConstants.DELIVERY_STATUS_PENDING, DiningConstants.DELIVERY_STATUS_FAILED)
        .orderByDesc(DiningDeliveryRecord::getCreateTime));
    for (DiningDeliveryRecord candidate : candidates) {
      DiningMealOrder order = resolveMealOrder(orgId, candidate.getMealOrderId());
      String residentQrCode = resolveResidentQrCode(order);
      if (qrCode.equals(residentQrCode)) {
        return candidate;
      }
    }
    return null;
  }

  private String resolveResidentQrCode(DiningMealOrder order) {
    if (order == null || order.getElderId() == null) {
      return null;
    }
    ElderProfile elder = elderMapper.selectOne(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getId, order.getElderId())
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(order.getOrgId() != null, ElderProfile::getOrgId, order.getOrgId())
        .last("LIMIT 1"));
    if (elder == null) {
      return null;
    }
    if (elder.getBedId() != null) {
      Bed bed = bedMapper.selectOne(Wrappers.lambdaQuery(Bed.class)
          .eq(Bed::getId, elder.getBedId())
          .eq(Bed::getIsDeleted, 0)
          .eq(order.getOrgId() != null, Bed::getOrgId, order.getOrgId())
          .last("LIMIT 1"));
      if (bed != null && bed.getBedQrCode() != null && !bed.getBedQrCode().isBlank()) {
        return bed.getBedQrCode().trim();
      }
    }
    return elder.getElderQrCode() == null ? null : elder.getElderQrCode().trim();
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

  private LocalDateTime resolveSignedAt(
      LocalDateTime signedAt,
      LocalDateTime qrScanAt,
      String status,
      LocalDateTime currentValue) {
    if (signedAt != null) {
      return signedAt;
    }
    if (qrScanAt != null) {
      return qrScanAt;
    }
    if (DiningConstants.DELIVERY_STATUS_DELIVERED.equals(status)) {
      return currentValue;
    }
    return currentValue;
  }

  private void hydrateRecords(List<DiningDeliveryRecord> records) {
    if (records == null || records.isEmpty()) {
      return;
    }
    records.forEach(this::hydrateRecord);
  }

  private void hydrateRecord(DiningDeliveryRecord record) {
    if (record == null) {
      return;
    }
    record.setSignoffImageUrls(splitImageUrls(record.getSignoffImageUrlsText()));
  }

  private String joinImageUrls(List<String> imageUrls) {
    if (imageUrls == null || imageUrls.isEmpty()) {
      return null;
    }
    List<String> normalized = imageUrls.stream()
        .map(this::normalizeText)
        .filter(item -> item != null)
        .distinct()
        .limit(9)
        .toList();
    if (normalized.isEmpty()) {
      return null;
    }
    return String.join("\n", normalized);
  }

  private List<String> splitImageUrls(String imageUrlsText) {
    if (imageUrlsText == null || imageUrlsText.isBlank()) {
      return Collections.emptyList();
    }
    return Arrays.stream(imageUrlsText.split("\\R+"))
        .map(this::normalizeText)
        .filter(item -> item != null)
        .distinct()
        .collect(Collectors.toList());
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

  private String resolveRedispatchStatus(String requestValue, String currentValue, boolean create) {
    String value = normalizeText(requestValue);
    if (value == null) {
      if (create) {
        return "NONE";
      }
      return currentValue == null ? "NONE" : currentValue;
    }
    if (!"NONE".equals(value) && !"REDISPATCHED".equals(value)) {
      throw new IllegalArgumentException("重派状态不合法");
    }
    return value;
  }
}
