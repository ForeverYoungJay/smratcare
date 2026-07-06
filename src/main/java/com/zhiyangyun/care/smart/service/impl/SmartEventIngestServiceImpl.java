package com.zhiyangyun.care.smart.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.smart.entity.SmartAlert;
import com.zhiyangyun.care.smart.entity.SmartAlertDispatch;
import com.zhiyangyun.care.smart.entity.SmartAlertRule;
import com.zhiyangyun.care.smart.entity.SmartDevice;
import com.zhiyangyun.care.smart.entity.SmartDeviceEvent;
import com.zhiyangyun.care.smart.mapper.SmartAlertMapper;
import com.zhiyangyun.care.smart.mapper.SmartAlertRuleMapper;
import com.zhiyangyun.care.smart.mapper.SmartDeviceEventMapper;
import com.zhiyangyun.care.smart.mapper.SmartDeviceMapper;
import com.zhiyangyun.care.smart.model.SmartEventIngestRequest;
import com.zhiyangyun.care.smart.model.SmartEventIngestResponse;
import com.zhiyangyun.care.smart.model.SmartRuleMatch;
import com.zhiyangyun.care.smart.service.SmartAlertRuleEngine;
import com.zhiyangyun.care.smart.service.SmartDispatchService;
import com.zhiyangyun.care.smart.service.SmartEventIngestService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class SmartEventIngestServiceImpl implements SmartEventIngestService {

  private static final String HEARTBEAT = "HEARTBEAT";
  private static final String LOW_BATTERY_ALERT_TYPE = "DEVICE_LOW_BATTERY";
  private static final DateTimeFormatter ALERT_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  private final SmartDeviceMapper deviceMapper;
  private final SmartDeviceEventMapper eventMapper;
  private final SmartAlertMapper alertMapper;
  private final SmartAlertRuleMapper ruleMapper;
  private final ElderMapper elderMapper;
  private final SmartAlertRuleEngine ruleEngine;
  private final SmartDispatchService dispatchService;
  private final ObjectMapper objectMapper;
  private final int lowBatteryPercent;

  public SmartEventIngestServiceImpl(
      SmartDeviceMapper deviceMapper,
      SmartDeviceEventMapper eventMapper,
      SmartAlertMapper alertMapper,
      SmartAlertRuleMapper ruleMapper,
      ElderMapper elderMapper,
      SmartAlertRuleEngine ruleEngine,
      SmartDispatchService dispatchService,
      ObjectMapper objectMapper,
      @Value("${zhiyangyun.smart.monitor.low-battery-percent:20}") int lowBatteryPercent) {
    this.deviceMapper = deviceMapper;
    this.eventMapper = eventMapper;
    this.alertMapper = alertMapper;
    this.ruleMapper = ruleMapper;
    this.elderMapper = elderMapper;
    this.ruleEngine = ruleEngine;
    this.dispatchService = dispatchService;
    this.objectMapper = objectMapper;
    this.lowBatteryPercent = lowBatteryPercent;
  }

  @Override
  @Transactional
  public SmartEventIngestResponse ingest(SmartEventIngestRequest request) {
    SmartDevice device = resolveDevice(request.getDeviceSn());
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime occurredAt = request.getOccurredAt() == null ? now : request.getOccurredAt();
    String eventType = SmartAlertRuleEngine.canonicalEventType(request.getEventType());
    Map<String, Double> metrics = extractMetrics(request.getPayload());
    Integer battery = firstNonNull(request.getBatteryLevel(), intMetric(metrics, "batteryLevel"));
    Integer signal = firstNonNull(request.getSignalStrength(), intMetric(metrics, "signalStrength"));

    refreshDeviceHealth(device, eventType, occurredAt, battery, signal, request.getFirmwareVersion(), now);

    SmartDeviceEvent event = saveEvent(device, request, eventType, occurredAt, now);
    SmartEventIngestResponse response = new SmartEventIngestResponse();
    response.setEventId(event.getId());
    response.setDeviceId(device.getId());

    if (battery != null && battery <= lowBatteryPercent) {
      raiseLowBatteryAlert(device, battery, now);
    }
    if (HEARTBEAT.equals(eventType)) {
      return response;
    }

    SmartRuleMatch match = matchRule(device, eventType, metrics);
    if (!match.isMatched()) {
      return response;
    }
    SmartAlert alert = createAlert(device, event, request, match, occurredAt, now);
    event.setGeneratedAlertId(alert.getId());
    event.setEventLevel(match.getLevel());
    event.setUpdateTime(now);
    eventMapper.updateById(event);
    response.setAlertCreated(true);
    response.setAlertId(alert.getId());
    response.setAlertLevel(alert.getLevel());
    response.setMatchedRuleCode(match.getRuleCode());

    if (match.isAutoDispatch()) {
      SmartAlertDispatch dispatch = dispatchService.createForAlert(alert, match.getRuleId());
      if (dispatch != null) {
        response.setDispatchCreated(true);
        response.setDispatchId(dispatch.getId());
      }
    }
    return response;
  }

  private SmartDevice resolveDevice(String deviceSn) {
    if (!StringUtils.hasText(deviceSn)) {
      throw new IllegalArgumentException("deviceSn不能为空");
    }
    List<SmartDevice> devices = deviceMapper.selectList(Wrappers.lambdaQuery(SmartDevice.class)
        .eq(SmartDevice::getIsDeleted, 0)
        .eq(SmartDevice::getDeviceCode, deviceSn.trim()));
    if (devices.isEmpty()) {
      throw new IllegalArgumentException("未登记的设备SN: " + deviceSn);
    }
    if (devices.size() > 1) {
      throw new IllegalArgumentException("设备SN在多个机构重复登记，无法路由: " + deviceSn);
    }
    SmartDevice device = devices.get(0);
    if (device.getEnabled() != null && device.getEnabled() == 0) {
      throw new IllegalArgumentException("设备已停用: " + deviceSn);
    }
    return device;
  }

  private void refreshDeviceHealth(SmartDevice device, String eventType, LocalDateTime occurredAt,
      Integer battery, Integer signal, String firmwareVersion, LocalDateTime now) {
    device.setOnlineStatus("ONLINE");
    if (HEARTBEAT.equals(eventType)) {
      device.setLastHeartbeatAt(occurredAt);
    } else {
      device.setLastEventAt(occurredAt);
    }
    if (battery != null) {
      device.setBatteryLevel(Math.max(0, Math.min(100, battery)));
    }
    if (signal != null) {
      device.setSignalStrength(Math.max(0, Math.min(100, signal)));
    }
    if (StringUtils.hasText(firmwareVersion)) {
      device.setFirmwareVersion(firmwareVersion.trim());
    }
    device.setUpdateTime(now);
    deviceMapper.updateById(device);
  }

  private SmartDeviceEvent saveEvent(SmartDevice device, SmartEventIngestRequest request,
      String eventType, LocalDateTime occurredAt, LocalDateTime now) {
    SmartDeviceEvent event = new SmartDeviceEvent();
    event.setOrgId(device.getOrgId());
    event.setTenantId(device.getTenantId() != null ? device.getTenantId() : device.getOrgId());
    event.setDeviceId(device.getId());
    event.setElderId(device.getElderId());
    event.setDeviceCode(device.getDeviceCode());
    event.setEventType(eventType);
    event.setEventLevel("INFO");
    event.setEventTime(occurredAt);
    event.setPayloadJson(toJson(request.getPayload()));
    event.setLocation(StringUtils.hasText(request.getLocation()) ? request.getLocation().trim() : device.getLocation());
    event.setHandled(0);
    event.setCreateTime(now);
    event.setUpdateTime(now);
    event.setIsDeleted(0);
    eventMapper.insert(event);
    return event;
  }

  private SmartRuleMatch matchRule(SmartDevice device, String eventType, Map<String, Double> metrics) {
    List<SmartAlertRule> rules = ruleMapper.selectList(Wrappers.lambdaQuery(SmartAlertRule.class)
        .eq(SmartAlertRule::getIsDeleted, 0)
        .eq(SmartAlertRule::getEnabled, 1)
        .and(w -> w.isNull(SmartAlertRule::getOrgId).or(q -> q.eq(SmartAlertRule::getOrgId, device.getOrgId()))));
    String careLevel = null;
    if (device.getElderId() != null) {
      ElderProfile elder = elderMapper.selectById(device.getElderId());
      careLevel = elder == null ? null : elder.getCareLevel();
    }
    Integer disabilityLevel = intMetric(metrics, "disabilityLevel");
    return ruleEngine.evaluate(eventType, device.getDeviceType(), metrics, disabilityLevel, careLevel, rules);
  }

  private SmartAlert createAlert(SmartDevice device, SmartDeviceEvent event, SmartEventIngestRequest request,
      SmartRuleMatch match, LocalDateTime occurredAt, LocalDateTime now) {
    SmartAlert alert = new SmartAlert();
    alert.setOrgId(device.getOrgId());
    alert.setTenantId(device.getTenantId() != null ? device.getTenantId() : device.getOrgId());
    alert.setElderId(device.getElderId());
    alert.setDeviceId(device.getId());
    alert.setSourceEventId(event.getId());
    alert.setAlertNo("SA-" + occurredAt.format(ALERT_NO_FORMATTER) + "-" + Math.abs(event.getId() % 100000));
    alert.setAlertType(event.getEventType());
    alert.setLevel(match.getLevel());
    alert.setTitle(device.getDeviceName() + " " + match.getTitle());
    alert.setContent(event.getPayloadJson());
    alert.setLocation(event.getLocation());
    alert.setMediaRef(trim(request.getMediaRef()));
    alert.setLocationRef(trim(request.getLocationRef()));
    alert.setStatus("OPEN");
    alert.setFirstTriggeredAt(occurredAt);
    alert.setLatestTriggeredAt(occurredAt);
    alert.setEscalationCount(0);
    alert.setNotifyFamily(match.isNotifyFamily() ? 1 : 0);
    alert.setCreateTime(now);
    alert.setUpdateTime(now);
    alert.setIsDeleted(0);
    alertMapper.insert(alert);
    return alert;
  }

  private void raiseLowBatteryAlert(SmartDevice device, int battery, LocalDateTime now) {
    Long open = alertMapper.selectCount(Wrappers.lambdaQuery(SmartAlert.class)
        .eq(SmartAlert::getIsDeleted, 0)
        .eq(SmartAlert::getDeviceId, device.getId())
        .eq(SmartAlert::getAlertType, LOW_BATTERY_ALERT_TYPE)
        .in(SmartAlert::getStatus, List.of("OPEN", "ACKNOWLEDGED")));
    if (open != null && open > 0) {
      return;
    }
    SmartAlert alert = new SmartAlert();
    alert.setOrgId(device.getOrgId());
    alert.setTenantId(device.getTenantId() != null ? device.getTenantId() : device.getOrgId());
    alert.setElderId(device.getElderId());
    alert.setDeviceId(device.getId());
    alert.setAlertNo("BAT" + now.format(ALERT_NO_FORMATTER) + device.getId());
    alert.setAlertType(LOW_BATTERY_ALERT_TYPE);
    alert.setLevel("WARNING");
    alert.setTitle("设备低电量");
    alert.setContent("设备[" + device.getDeviceName() + "]电量 " + battery + "%，低于阈值 " + lowBatteryPercent + "%，请及时充电/更换电池");
    alert.setLocation(device.getLocation());
    alert.setStatus("OPEN");
    alert.setFirstTriggeredAt(now);
    alert.setLatestTriggeredAt(now);
    alert.setEscalationCount(0);
    alert.setNotifyFamily(0);
    alert.setCreateTime(now);
    alert.setUpdateTime(now);
    alert.setIsDeleted(0);
    alertMapper.insert(alert);
  }

  private Map<String, Double> extractMetrics(JsonNode payload) {
    Map<String, Double> metrics = new HashMap<>();
    if (payload == null || !payload.isObject()) {
      return metrics;
    }
    Iterator<Map.Entry<String, JsonNode>> fields = payload.fields();
    while (fields.hasNext()) {
      Map.Entry<String, JsonNode> entry = fields.next();
      if (entry.getValue() != null && entry.getValue().isNumber()) {
        metrics.put(entry.getKey(), entry.getValue().asDouble());
      }
    }
    return metrics;
  }

  private Integer intMetric(Map<String, Double> metrics, String key) {
    Double value = metrics.get(key);
    return value == null ? null : (int) Math.round(value);
  }

  private Integer firstNonNull(Integer a, Integer b) {
    return a != null ? a : b;
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

  private String trim(String value) {
    return StringUtils.hasText(value) ? value.trim() : null;
  }
}
