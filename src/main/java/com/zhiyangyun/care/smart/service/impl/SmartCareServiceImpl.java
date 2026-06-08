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
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class SmartCareServiceImpl implements SmartCareService {
  private static final List<String> ALERT_LEVELS = List.of("WARNING", "HIGH", "CRITICAL");
  private static final DateTimeFormatter ALERT_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  private final SmartDeviceMapper smartDeviceMapper;
  private final SmartDeviceEventMapper smartDeviceEventMapper;
  private final SmartAlertMapper smartAlertMapper;
  private final ObjectMapper objectMapper;
  private final AuditLogService auditLogService;

  public SmartCareServiceImpl(
      SmartDeviceMapper smartDeviceMapper,
      SmartDeviceEventMapper smartDeviceEventMapper,
      SmartAlertMapper smartAlertMapper,
      ObjectMapper objectMapper,
      AuditLogService auditLogService) {
    this.smartDeviceMapper = smartDeviceMapper;
    this.smartDeviceEventMapper = smartDeviceEventMapper;
    this.smartAlertMapper = smartAlertMapper;
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
