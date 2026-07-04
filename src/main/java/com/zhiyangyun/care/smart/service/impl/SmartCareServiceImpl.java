package com.zhiyangyun.care.smart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.health.entity.HealthDataRecord;
import com.zhiyangyun.care.health.mapper.HealthDataRecordMapper;
import com.zhiyangyun.care.smart.entity.SmartAlert;
import com.zhiyangyun.care.smart.entity.SmartDevice;
import com.zhiyangyun.care.smart.entity.SmartDeviceEvent;
import com.zhiyangyun.care.smart.mapper.SmartAlertMapper;
import com.zhiyangyun.care.smart.mapper.SmartDeviceEventMapper;
import com.zhiyangyun.care.smart.mapper.SmartDeviceMapper;
import com.zhiyangyun.care.smart.model.SmartAlertResolveRequest;
import com.zhiyangyun.care.smart.model.SmartAlertSummaryResponse;
import com.zhiyangyun.care.smart.model.SmartDeviceEventRequest;
import com.zhiyangyun.care.smart.model.SmartDeviceRequest;
import com.zhiyangyun.care.smart.service.SmartCareService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class SmartCareServiceImpl implements SmartCareService {
  private static final List<String> ALERT_LEVELS = List.of("WARNING", "HIGH", "CRITICAL");
  private static final String DERIVED_HEALTH_TREND = "DERIVED_HEALTH_TREND";
  private static final DateTimeFormatter ALERT_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  private final SmartDeviceMapper smartDeviceMapper;
  private final SmartDeviceEventMapper smartDeviceEventMapper;
  private final SmartAlertMapper smartAlertMapper;
  private final HealthDataRecordMapper healthDataRecordMapper;
  private final ObjectMapper objectMapper;
  private final AuditLogService auditLogService;

  public SmartCareServiceImpl(
      SmartDeviceMapper smartDeviceMapper,
      SmartDeviceEventMapper smartDeviceEventMapper,
      SmartAlertMapper smartAlertMapper,
      HealthDataRecordMapper healthDataRecordMapper,
      ObjectMapper objectMapper,
      AuditLogService auditLogService) {
    this.smartDeviceMapper = smartDeviceMapper;
    this.smartDeviceEventMapper = smartDeviceEventMapper;
    this.smartAlertMapper = smartAlertMapper;
    this.healthDataRecordMapper = healthDataRecordMapper;
    this.objectMapper = objectMapper;
    this.auditLogService = auditLogService;
  }

  @Override
  public IPage<SmartDevice> pageDevices(int pageNo, int pageSize, String keyword, String deviceType, String onlineStatus) {
    Long orgId = AuthContext.getOrgId();
    LambdaQueryWrapper<SmartDevice> query = Wrappers.lambdaQuery(SmartDevice.class)
        .eq(SmartDevice::getIsDeleted, 0)
        .eq(orgId != null, SmartDevice::getOrgId, orgId)
        .eq(StringUtils.hasText(deviceType), SmartDevice::getDeviceType, normalize(deviceType))
        .eq(StringUtils.hasText(onlineStatus), SmartDevice::getOnlineStatus, normalize(onlineStatus))
        .and(StringUtils.hasText(keyword), wrapper -> wrapper
            .like(SmartDevice::getDeviceName, keyword)
            .or()
            .like(SmartDevice::getDeviceCode, keyword)
            .or()
            .like(SmartDevice::getLocation, keyword))
        .orderByDesc(SmartDevice::getUpdateTime);
    return smartDeviceMapper.selectPage(new Page<>(Math.max(1, pageNo), clampPageSize(pageSize)), query);
  }

  @Override
  @Transactional
  public SmartDevice saveDevice(Long id, SmartDeviceRequest request) {
    Long orgId = requireOrgId();
    LocalDateTime now = LocalDateTime.now();
    SmartDevice device = id == null ? null : smartDeviceMapper.selectById(id);
    if (device == null) {
      device = new SmartDevice();
      device.setOrgId(orgId);
      device.setTenantId(orgId);
      device.setCreatedBy(AuthContext.getStaffId());
      device.setCreateTime(now);
      device.setIsDeleted(0);
      device.setOnlineStatus("OFFLINE");
    } else {
      AuthContext.requireOrgAccess(device.getOrgId());
    }
    device.setElderId(request.getElderId());
    device.setDeviceCode(requiredText(request.getDeviceCode(), "deviceCode"));
    device.setDeviceName(requiredText(request.getDeviceName(), "deviceName"));
    device.setDeviceType(normalize(requiredText(request.getDeviceType(), "deviceType")));
    device.setVendor(trim(request.getVendor()));
    device.setModel(trim(request.getModel()));
    device.setLocation(trim(request.getLocation()));
    device.setProtocol(normalize(request.getProtocol()));
    device.setBindStatus(StringUtils.hasText(request.getBindStatus()) ? normalize(request.getBindStatus()) : "BOUND");
    device.setEnabled(request.getEnabled() == null ? 1 : (request.getEnabled() == 0 ? 0 : 1));
    device.setRemark(trim(request.getRemark()));
    device.setUpdateTime(now);
    if (device.getId() == null) {
      smartDeviceMapper.insert(device);
    } else {
      smartDeviceMapper.updateById(device);
    }
    auditLogService.recordStructured(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        id == null ? "SMART_DEVICE_CREATE" : "SMART_DEVICE_UPDATE", "SMART_DEVICE", device.getId(),
        "智慧设备档案保存", null, device, null);
    return device;
  }

  @Override
  @Transactional
  public SmartDevice setDeviceEnabled(Long id, boolean enabled) {
    SmartDevice device = requireDevice(id);
    device.setEnabled(enabled ? 1 : 0);
    device.setUpdateTime(LocalDateTime.now());
    smartDeviceMapper.updateById(device);
    auditLogService.record(device.getOrgId(), device.getOrgId(), AuthContext.getStaffId(), AuthContext.getUsername(),
        enabled ? "SMART_DEVICE_ENABLE" : "SMART_DEVICE_DISABLE", "SMART_DEVICE", id, "智慧设备启停");
    return device;
  }

  @Override
  @Transactional
  public SmartDeviceEvent reportEvent(SmartDeviceEventRequest request) {
    Long orgId = requireOrgId();
    LocalDateTime now = LocalDateTime.now();
    SmartDevice device = smartDeviceMapper.selectOne(Wrappers.lambdaQuery(SmartDevice.class)
        .eq(SmartDevice::getIsDeleted, 0)
        .eq(SmartDevice::getOrgId, orgId)
        .eq(SmartDevice::getDeviceCode, requiredText(request.getDeviceCode(), "deviceCode"))
        .last("LIMIT 1"));
    LocalDateTime eventTime = request.getEventTime() == null ? now : request.getEventTime();
    String eventLevel = normalize(defaultText(request.getEventLevel(), "INFO"));

    SmartDeviceEvent event = new SmartDeviceEvent();
    event.setOrgId(orgId);
    event.setTenantId(orgId);
    event.setDeviceId(device == null ? null : device.getId());
    event.setElderId(request.getElderId() != null ? request.getElderId() : (device == null ? null : device.getElderId()));
    event.setDeviceCode(requiredText(request.getDeviceCode(), "deviceCode"));
    event.setEventType(normalize(requiredText(request.getEventType(), "eventType")));
    event.setEventLevel(eventLevel);
    event.setEventTime(eventTime);
    event.setPayloadJson(toJson(request.getPayload()));
    event.setLocation(StringUtils.hasText(request.getLocation()) ? trim(request.getLocation()) : (device == null ? null : device.getLocation()));
    event.setHandled(0);
    event.setCreatedBy(AuthContext.getStaffId());
    event.setCreateTime(now);
    event.setUpdateTime(now);
    event.setIsDeleted(0);
    smartDeviceEventMapper.insert(event);

    if (device != null) {
      device.setOnlineStatus("ONLINE");
      device.setLastEventAt(eventTime);
      device.setLastHeartbeatAt("HEARTBEAT".equals(event.getEventType()) ? eventTime : device.getLastHeartbeatAt());
      device.setUpdateTime(now);
      smartDeviceMapper.updateById(device);
    }
    if (ALERT_LEVELS.contains(eventLevel)) {
      SmartAlert alert = createAlert(event, device);
      event.setGeneratedAlertId(alert.getId());
      smartDeviceEventMapper.updateById(event);
    }
    return event;
  }

  @Override
  public SmartAlertSummaryResponse summary() {
    Long orgId = AuthContext.getOrgId();
    SmartAlertSummaryResponse response = new SmartAlertSummaryResponse();
    response.setTotalDeviceCount(countDevices(orgId, null));
    response.setOnlineDeviceCount(countDevices(orgId, "ONLINE"));
    response.setOfflineDeviceCount(countDevices(orgId, "OFFLINE"));
    response.setOpenAlertCount(countAlerts(orgId, "OPEN", null));
    response.setCriticalAlertCount(countAlerts(orgId, "OPEN", "CRITICAL"));
    response.setDerivedHealthAlertCount(countDerivedHealthAlerts(orgId));
    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    response.setTodayEventCount(smartDeviceEventMapper.selectCount(Wrappers.lambdaQuery(SmartDeviceEvent.class)
        .eq(SmartDeviceEvent::getIsDeleted, 0)
        .eq(orgId != null, SmartDeviceEvent::getOrgId, orgId)
        .ge(SmartDeviceEvent::getEventTime, startOfDay)));
    response.setLevelStats(List.of(
        new SmartAlertSummaryResponse.LevelCount("CRITICAL", countAlerts(orgId, "OPEN", "CRITICAL")),
        new SmartAlertSummaryResponse.LevelCount("HIGH", countAlerts(orgId, "OPEN", "HIGH")),
        new SmartAlertSummaryResponse.LevelCount("WARNING", countAlerts(orgId, "OPEN", "WARNING"))));
    return response;
  }

  @Override
  @Transactional
  public SmartAlertSummaryResponse refreshDerivedHealthAlerts() {
    Long orgId = requireOrgId();
    LocalDateTime since = LocalDateTime.now().minusDays(7);
    List<HealthDataRecord> records = healthDataRecordMapper.selectList(Wrappers.lambdaQuery(HealthDataRecord.class)
        .eq(HealthDataRecord::getIsDeleted, 0)
        .eq(HealthDataRecord::getOrgId, orgId)
        .eq(HealthDataRecord::getAbnormalFlag, 1)
        .ge(HealthDataRecord::getMeasuredAt, since)
        .orderByDesc(HealthDataRecord::getMeasuredAt));
    Map<String, List<HealthDataRecord>> groups = new LinkedHashMap<>();
    for (HealthDataRecord record : records) {
      if (record.getElderId() == null || record.getMeasuredAt() == null) {
        continue;
      }
      String metricKey = normalizeHealthMetric(record.getDataType());
      if (metricKey == null) {
        continue;
      }
      groups.computeIfAbsent(record.getElderId() + "#" + metricKey, key -> new java.util.ArrayList<>()).add(record);
    }
    long generatedCount = 0L;
    for (Map.Entry<String, List<HealthDataRecord>> entry : groups.entrySet()) {
      List<HealthDataRecord> group = entry.getValue();
      Set<LocalDate> measuredDates = new HashSet<>();
      for (HealthDataRecord record : group) {
        measuredDates.add(record.getMeasuredAt().toLocalDate());
      }
      if (group.size() >= 3 && measuredDates.size() >= 2 && upsertDerivedHealthAlert(orgId, entry.getKey(), group)) {
        generatedCount++;
      }
    }
    SmartAlertSummaryResponse response = summary();
    response.setDerivedHealthGeneratedCount(generatedCount);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "SMART_ALERT_DERIVED_HEALTH_REFRESH", "SMART_ALERT", null, "刷新连续健康异常趋势告警");
    return response;
  }

  @Override
  public IPage<SmartAlert> pageAlerts(int pageNo, int pageSize, String status, String level, Long elderId, String keyword) {
    Long orgId = AuthContext.getOrgId();
    LambdaQueryWrapper<SmartAlert> query = Wrappers.lambdaQuery(SmartAlert.class)
        .eq(SmartAlert::getIsDeleted, 0)
        .eq(orgId != null, SmartAlert::getOrgId, orgId)
        .eq(StringUtils.hasText(status), SmartAlert::getStatus, normalize(status))
        .eq(StringUtils.hasText(level), SmartAlert::getLevel, normalize(level))
        .eq(elderId != null, SmartAlert::getElderId, elderId)
        .and(StringUtils.hasText(keyword), wrapper -> wrapper
            .like(SmartAlert::getTitle, keyword)
            .or()
            .like(SmartAlert::getContent, keyword)
            .or()
            .like(SmartAlert::getLocation, keyword))
        .orderByDesc(SmartAlert::getLatestTriggeredAt);
    return smartAlertMapper.selectPage(new Page<>(Math.max(1, pageNo), clampPageSize(pageSize)), query);
  }

  @Override
  @Transactional
  public SmartAlert acknowledgeAlert(Long id) {
    SmartAlert alert = requireAlert(id);
    if (!"RESOLVED".equals(alert.getStatus())) {
      alert.setStatus("ACKNOWLEDGED");
      alert.setAcknowledgedBy(AuthContext.getStaffId());
      alert.setAcknowledgedAt(LocalDateTime.now());
      smartAlertMapper.updateById(alert);
    }
    auditLogService.record(alert.getOrgId(), alert.getOrgId(), AuthContext.getStaffId(), AuthContext.getUsername(),
        "SMART_ALERT_ACK", "SMART_ALERT", id, "智慧告警确认");
    return alert;
  }

  @Override
  @Transactional
  public SmartAlert resolveAlert(Long id, SmartAlertResolveRequest request) {
    SmartAlert alert = requireAlert(id);
    alert.setStatus("RESOLVED");
    alert.setResolvedBy(AuthContext.getStaffId());
    alert.setResolvedAt(LocalDateTime.now());
    alert.setResolutionNote(trim(request == null ? null : request.getResolutionNote()));
    alert.setNotifyFamily(Boolean.TRUE.equals(request == null ? null : request.getNotifyFamily()) ? 1 : alert.getNotifyFamily());
    smartAlertMapper.updateById(alert);
    if (alert.getSourceEventId() != null) {
      SmartDeviceEvent event = smartDeviceEventMapper.selectById(alert.getSourceEventId());
      if (event != null) {
        event.setHandled(1);
        event.setUpdateTime(LocalDateTime.now());
        smartDeviceEventMapper.updateById(event);
      }
    }
    auditLogService.recordStructured(alert.getOrgId(), alert.getOrgId(), AuthContext.getStaffId(), AuthContext.getUsername(),
        "SMART_ALERT_RESOLVE", "SMART_ALERT", id, "智慧告警处置", null, alert, request);
    return alert;
  }

  private SmartAlert createAlert(SmartDeviceEvent event, SmartDevice device) {
    SmartAlert alert = new SmartAlert();
    alert.setOrgId(event.getOrgId());
    alert.setTenantId(event.getOrgId());
    alert.setElderId(event.getElderId());
    alert.setDeviceId(device == null ? null : device.getId());
    alert.setSourceEventId(event.getId());
    alert.setAlertNo("SA-" + event.getEventTime().format(ALERT_NO_FORMATTER) + "-" + Math.abs(event.getId() % 100000));
    alert.setAlertType(event.getEventType());
    alert.setLevel(event.getEventLevel());
    alert.setTitle(resolveAlertTitle(event, device));
    alert.setContent(event.getPayloadJson());
    alert.setLocation(event.getLocation());
    alert.setStatus("OPEN");
    alert.setFirstTriggeredAt(event.getEventTime());
    alert.setLatestTriggeredAt(event.getEventTime());
    alert.setEscalationCount(0);
    alert.setNotifyFamily("CRITICAL".equals(event.getEventLevel()) ? 1 : 0);
    alert.setCreatedBy(event.getCreatedBy());
    alert.setCreateTime(LocalDateTime.now());
    alert.setUpdateTime(LocalDateTime.now());
    alert.setIsDeleted(0);
    smartAlertMapper.insert(alert);
    return alert;
  }

  private boolean upsertDerivedHealthAlert(Long orgId, String groupKey, List<HealthDataRecord> group) {
    String[] keyParts = groupKey.split("#", 2);
    Long elderId = Long.valueOf(keyParts[0]);
    String metricKey = keyParts[1];
    String metricLabel = healthMetricLabel(metricKey);
    HealthDataRecord latestRecord = group.stream()
        .max(Comparator.comparing(HealthDataRecord::getMeasuredAt))
        .orElse(group.get(0));
    LocalDateTime firstTriggeredAt = group.stream()
        .map(HealthDataRecord::getMeasuredAt)
        .min(LocalDateTime::compareTo)
        .orElse(latestRecord.getMeasuredAt());
    LocalDateTime latestTriggeredAt = latestRecord.getMeasuredAt();
    String content = buildDerivedHealthPayload(metricKey, metricLabel, group, latestRecord);
    SmartAlert existing = smartAlertMapper.selectOne(Wrappers.lambdaQuery(SmartAlert.class)
        .eq(SmartAlert::getIsDeleted, 0)
        .eq(SmartAlert::getOrgId, orgId)
        .eq(SmartAlert::getElderId, elderId)
        .eq(SmartAlert::getAlertType, DERIVED_HEALTH_TREND)
        .ne(SmartAlert::getStatus, "RESOLVED")
        .like(SmartAlert::getContent, "\"metricKey\":\"" + metricKey + "\"")
        .last("LIMIT 1"));
    LocalDateTime now = LocalDateTime.now();
    if (existing != null) {
      existing.setStatus("OPEN");
      existing.setLevel(derivedHealthLevel(metricKey));
      existing.setTitle(metricLabel + "连续异常预警");
      existing.setContent(content);
      existing.setLocation("健康数据趋势");
      existing.setLatestTriggeredAt(latestTriggeredAt);
      existing.setEscalationCount((existing.getEscalationCount() == null ? 0 : existing.getEscalationCount()) + 1);
      existing.setNotifyFamily("HIGH".equals(existing.getLevel()) ? 1 : existing.getNotifyFamily());
      existing.setUpdateTime(now);
      smartAlertMapper.updateById(existing);
      return true;
    }
    SmartAlert alert = new SmartAlert();
    alert.setOrgId(orgId);
    alert.setTenantId(orgId);
    alert.setElderId(elderId);
    alert.setAlertNo("SA-" + now.format(ALERT_NO_FORMATTER) + "-HT-" + Math.abs(groupKey.hashCode() % 10000));
    alert.setAlertType(DERIVED_HEALTH_TREND);
    alert.setLevel(derivedHealthLevel(metricKey));
    alert.setTitle(metricLabel + "连续异常预警");
    alert.setContent(content);
    alert.setLocation("健康数据趋势");
    alert.setStatus("OPEN");
    alert.setFirstTriggeredAt(firstTriggeredAt);
    alert.setLatestTriggeredAt(latestTriggeredAt);
    alert.setEscalationCount(0);
    alert.setNotifyFamily("HIGH".equals(alert.getLevel()) ? 1 : 0);
    alert.setCreatedBy(AuthContext.getStaffId());
    alert.setCreateTime(now);
    alert.setUpdateTime(now);
    alert.setIsDeleted(0);
    smartAlertMapper.insert(alert);
    return true;
  }

  private String buildDerivedHealthPayload(String metricKey, String metricLabel, List<HealthDataRecord> group, HealthDataRecord latestRecord) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("source", "health_data_record");
    payload.put("metricKey", metricKey);
    payload.put("metricLabel", metricLabel);
    payload.put("elderName", latestRecord.getElderName());
    payload.put("abnormalCount", group.size());
    payload.put("latestValue", latestRecord.getDataValue());
    payload.put("latestMeasuredAt", latestRecord.getMeasuredAt());
    payload.put("suggestion", "请安排复测、查看医嘱和护理观察记录，必要时同步家属。");
    return toJson(payload);
  }

  private String resolveAlertTitle(SmartDeviceEvent event, SmartDevice device) {
    String deviceName = device == null ? event.getDeviceCode() : device.getDeviceName();
    return switch (event.getEventType()) {
      case "FALL" -> deviceName + " 触发跌倒告警";
      case "SOS" -> deviceName + " 触发紧急呼叫";
      case "VITAL_ABNORMAL" -> deviceName + " 触发生命体征异常";
      case "GEOFENCE" -> deviceName + " 触发电子围栏告警";
      default -> deviceName + " 触发" + event.getEventLevel() + "告警";
    };
  }

  private SmartDevice requireDevice(Long id) {
    SmartDevice device = smartDeviceMapper.selectById(id);
    if (device == null || device.getIsDeleted() != null && device.getIsDeleted() != 0) {
      throw new IllegalArgumentException("设备不存在");
    }
    AuthContext.requireOrgAccess(device.getOrgId());
    return device;
  }

  private SmartAlert requireAlert(Long id) {
    SmartAlert alert = smartAlertMapper.selectById(id);
    if (alert == null || alert.getIsDeleted() != null && alert.getIsDeleted() != 0) {
      throw new IllegalArgumentException("告警不存在");
    }
    AuthContext.requireOrgAccess(alert.getOrgId());
    return alert;
  }

  private Long countDevices(Long orgId, String onlineStatus) {
    return smartDeviceMapper.selectCount(Wrappers.lambdaQuery(SmartDevice.class)
        .eq(SmartDevice::getIsDeleted, 0)
        .eq(orgId != null, SmartDevice::getOrgId, orgId)
        .eq(StringUtils.hasText(onlineStatus), SmartDevice::getOnlineStatus, onlineStatus));
  }

  private Long countAlerts(Long orgId, String status, String level) {
    return smartAlertMapper.selectCount(Wrappers.lambdaQuery(SmartAlert.class)
        .eq(SmartAlert::getIsDeleted, 0)
        .eq(orgId != null, SmartAlert::getOrgId, orgId)
        .eq(StringUtils.hasText(status), SmartAlert::getStatus, status)
        .eq(StringUtils.hasText(level), SmartAlert::getLevel, level));
  }

  private Long countDerivedHealthAlerts(Long orgId) {
    return smartAlertMapper.selectCount(Wrappers.lambdaQuery(SmartAlert.class)
        .eq(SmartAlert::getIsDeleted, 0)
        .eq(orgId != null, SmartAlert::getOrgId, orgId)
        .eq(SmartAlert::getAlertType, DERIVED_HEALTH_TREND)
        .ne(SmartAlert::getStatus, "RESOLVED"));
  }

  private Long requireOrgId() {
    Long orgId = AuthContext.getOrgId();
    if (orgId == null) {
      throw new IllegalArgumentException("当前账号缺少机构信息");
    }
    return orgId;
  }

  private int clampPageSize(int pageSize) {
    return Math.min(Math.max(pageSize <= 0 ? 10 : pageSize, 1), 200);
  }

  private String toJson(JsonNode node) {
    if (node == null || node.isNull()) {
      return null;
    }
    try {
      return objectMapper.writeValueAsString(node);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("事件载荷不是有效 JSON");
    }
  }

  private String toJson(Map<String, Object> payload) {
    try {
      return objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("告警载荷不是有效 JSON");
    }
  }

  private String normalizeHealthMetric(String value) {
    String text = trim(value);
    if (text == null) {
      return null;
    }
    String lower = text.toLowerCase(Locale.ROOT);
    if (lower.contains("bp") || text.contains("血压") || lower.contains("pressure")) {
      return "BP";
    }
    if (lower.contains("glucose") || lower.contains("sugar") || text.contains("血糖")) {
      return "SUGAR";
    }
    if (lower.contains("temp") || text.contains("体温")) {
      return "TEMP";
    }
    if (lower.contains("heart") || lower.contains("hr") || text.contains("心率") || text.contains("脉搏")) {
      return "HR";
    }
    if (lower.contains("spo2") || lower.contains("oxygen") || text.contains("血氧")) {
      return "SPO2";
    }
    return null;
  }

  private String healthMetricLabel(String metricKey) {
    return switch (metricKey) {
      case "BP" -> "血压";
      case "SUGAR" -> "血糖";
      case "TEMP" -> "体温";
      case "HR" -> "心率";
      case "SPO2" -> "血氧";
      default -> "健康指标";
    };
  }

  private String derivedHealthLevel(String metricKey) {
    return switch (metricKey) {
      case "BP", "SUGAR", "SPO2" -> "HIGH";
      default -> "WARNING";
    };
  }

  private String normalize(String value) {
    return trim(value) == null ? null : trim(value).toUpperCase(Locale.ROOT);
  }

  private String requiredText(String value, String fieldName) {
    if (!StringUtils.hasText(value)) {
      throw new IllegalArgumentException(fieldName + "不能为空");
    }
    return value.trim();
  }

  private String defaultText(String value, String fallback) {
    return StringUtils.hasText(value) ? value.trim() : fallback;
  }

  private String trim(String value) {
    return StringUtils.hasText(value) ? value.trim() : null;
  }
}
