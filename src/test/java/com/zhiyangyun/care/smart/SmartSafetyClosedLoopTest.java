package com.zhiyangyun.care.smart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zhiyangyun.care.life.entity.IncidentReport;
import com.zhiyangyun.care.life.mapper.IncidentReportMapper;
import com.zhiyangyun.care.smart.entity.SmartAlert;
import com.zhiyangyun.care.smart.entity.SmartAlertDispatch;
import com.zhiyangyun.care.smart.entity.SmartDevice;
import com.zhiyangyun.care.smart.mapper.SmartAlertDispatchMapper;
import com.zhiyangyun.care.smart.mapper.SmartAlertMapper;
import com.zhiyangyun.care.smart.mapper.SmartDeviceMapper;
import com.zhiyangyun.care.smart.model.SmartDispatchActionRequest;
import com.zhiyangyun.care.smart.model.SmartEventIngestRequest;
import com.zhiyangyun.care.smart.model.SmartEventIngestResponse;
import com.zhiyangyun.care.smart.service.SmartDispatchService;
import com.zhiyangyun.care.smart.service.SmartEventIngestService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * IoT 安全预警闭环集成测试（H2）：
 * 标准事件接入 → 场景规则匹配 → 告警 → 自动派单当班护理员 → 响应/到场/处置(一键生成不良事件)/复盘 → 超时升级部长。
 */
@SpringBootTest
@ActiveProfiles("test")
class SmartSafetyClosedLoopTest {

  private static final Long ORG_ID = 1L;
  private static final Long ELDER_ID = 200L;

  @Autowired
  private SmartEventIngestService ingestService;
  @Autowired
  private SmartDispatchService dispatchService;
  @Autowired
  private SmartDeviceMapper deviceMapper;
  @Autowired
  private SmartAlertMapper alertMapper;
  @Autowired
  private SmartAlertDispatchMapper dispatchMapper;
  @Autowired
  private IncidentReportMapper incidentReportMapper;
  @Autowired
  private ObjectMapper objectMapper;

  private SmartDevice createDevice(String sn) {
    LocalDateTime now = LocalDateTime.now();
    SmartDevice device = new SmartDevice();
    device.setOrgId(ORG_ID);
    device.setTenantId(ORG_ID);
    device.setElderId(ELDER_ID);
    device.setDeviceCode(sn);
    device.setDeviceName("测试手环" + sn);
    device.setDeviceType("WEARABLE");
    device.setLocation("101房间");
    device.setBindStatus("BOUND");
    device.setOnlineStatus("OFFLINE");
    device.setEnabled(1);
    device.setCreateTime(now);
    device.setUpdateTime(now);
    device.setIsDeleted(0);
    deviceMapper.insert(device);
    return device;
  }

  @Test
  void fall_event_flows_through_closed_loop() {
    SmartDevice device = createDevice("SN-FALL-" + System.nanoTime());

    SmartEventIngestRequest request = new SmartEventIngestRequest();
    request.setDeviceSn(device.getDeviceCode());
    request.setEventType("FALL");
    request.setMediaRef("vendor://clip/123");
    request.setLocationRef("vendor://track/456");
    ObjectNode payload = objectMapper.createObjectNode();
    payload.put("impact", 3.2);
    request.setPayload(payload);

    SmartEventIngestResponse response = ingestService.ingest(request);

    assertNotNull(response.getEventId());
    assertTrue(response.isAlertCreated());
    assertEquals("CRITICAL", response.getAlertLevel());
    assertEquals("FALL", response.getMatchedRuleCode());
    assertTrue(response.isDispatchCreated());

    SmartDevice refreshed = deviceMapper.selectById(device.getId());
    assertEquals("ONLINE", refreshed.getOnlineStatus());
    assertNotNull(refreshed.getLastEventAt());

    SmartAlert alert = alertMapper.selectById(response.getAlertId());
    assertEquals("vendor://clip/123", alert.getMediaRef());
    assertEquals("vendor://track/456", alert.getLocationRef());

    // 自动派单指派当班护理员（排班表：护理员甲今日全天当班）
    SmartAlertDispatch dispatch = dispatchMapper.selectById(response.getDispatchId());
    assertEquals("ASSIGNED", dispatch.getDispatchStatus());
    assertEquals("值班护理员甲", dispatch.getAssigneeName());
    assertNotNull(dispatch.getResponseDeadline());

    // 响应 → 到场 → 处置(一键生成不良事件) → 复盘
    dispatchService.respond(dispatch.getId());
    dispatchService.onsite(dispatch.getId());

    SmartDispatchActionRequest handleRequest = new SmartDispatchActionRequest();
    handleRequest.setDispatchId(dispatch.getId());
    handleRequest.setNote("到场扶起，测量体征平稳");
    handleRequest.setCreateIncident(true);
    SmartAlertDispatch handled = dispatchService.handle(handleRequest);
    assertEquals("HANDLED", handled.getDispatchStatus());
    assertNotNull(handled.getIncidentId());

    IncidentReport incident = incidentReportMapper.selectById(handled.getIncidentId());
    assertNotNull(incident);
    assertEquals("MAJOR", incident.getLevel());
    assertEquals("Elder One", incident.getElderName());
    assertEquals("OPEN", incident.getStatus());

    SmartDispatchActionRequest reviewRequest = new SmartDispatchActionRequest();
    reviewRequest.setDispatchId(dispatch.getId());
    reviewRequest.setNote("复盘：加强夜间巡查");
    SmartAlertDispatch reviewed = dispatchService.review(reviewRequest);
    assertEquals("REVIEWED", reviewed.getDispatchStatus());
    assertNotNull(reviewed.getReviewedAt());
  }

  @Test
  void heartbeat_refreshes_health_and_raises_low_battery_alert() {
    SmartDevice device = createDevice("SN-HB-" + System.nanoTime());

    SmartEventIngestRequest request = new SmartEventIngestRequest();
    request.setDeviceSn(device.getDeviceCode());
    request.setEventType("HEARTBEAT");
    request.setBatteryLevel(12);
    request.setSignalStrength(66);
    request.setFirmwareVersion("1.0.3");

    SmartEventIngestResponse response = ingestService.ingest(request);
    assertFalse(response.isAlertCreated());

    SmartDevice refreshed = deviceMapper.selectById(device.getId());
    assertEquals("ONLINE", refreshed.getOnlineStatus());
    assertNotNull(refreshed.getLastHeartbeatAt());
    assertEquals(12, refreshed.getBatteryLevel());
    assertEquals(66, refreshed.getSignalStrength());
    assertEquals("1.0.3", refreshed.getFirmwareVersion());

    Long lowBatteryAlerts = alertMapper.selectCount(Wrappers.lambdaQuery(SmartAlert.class)
        .eq(SmartAlert::getIsDeleted, 0)
        .eq(SmartAlert::getDeviceId, device.getId())
        .eq(SmartAlert::getAlertType, "DEVICE_LOW_BATTERY")
        .in(SmartAlert::getStatus, List.of("OPEN", "ACKNOWLEDGED")));
    assertEquals(1L, lowBatteryAlerts);

    // 再次心跳不重复产生低电量告警
    ingestService.ingest(request);
    Long afterSecond = alertMapper.selectCount(Wrappers.lambdaQuery(SmartAlert.class)
        .eq(SmartAlert::getIsDeleted, 0)
        .eq(SmartAlert::getDeviceId, device.getId())
        .eq(SmartAlert::getAlertType, "DEVICE_LOW_BATTERY")
        .in(SmartAlert::getStatus, List.of("OPEN", "ACKNOWLEDGED")));
    assertEquals(1L, afterSecond);
  }

  @Test
  void overdue_dispatch_escalates_to_minister() {
    SmartDevice device = createDevice("SN-SOS-" + System.nanoTime());

    SmartEventIngestRequest request = new SmartEventIngestRequest();
    request.setDeviceSn(device.getDeviceCode());
    request.setEventType("SOS");
    SmartEventIngestResponse response = ingestService.ingest(request);
    assertTrue(response.isDispatchCreated());

    SmartAlertDispatch dispatch = dispatchMapper.selectById(response.getDispatchId());
    dispatch.setResponseDeadline(LocalDateTime.now().minusMinutes(1));
    dispatchMapper.updateById(dispatch);

    // 定时任务可能抢先升级，这里显式再扫一次，最终按派单状态断言
    dispatchService.escalateOverdue();

    SmartAlertDispatch refreshed = dispatchMapper.selectById(dispatch.getId());
    // SmartDispatchScheduler 定时升级也可能在测试窗口内触发，故断言至少升级一次
    assertTrue(refreshed.getEscalationCount() >= 1);
    assertEquals("护理部长乙", refreshed.getEscalatedToName());
    assertEquals("护理部长乙", refreshed.getAssigneeName());
    assertNotNull(refreshed.getEscalatedAt());
    assertTrue(refreshed.getResponseDeadline().isAfter(LocalDateTime.now().minusSeconds(5)));
  }
}
