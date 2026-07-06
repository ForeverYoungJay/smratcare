package com.zhiyangyun.care.smart.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.smart.entity.SmartAlert;
import com.zhiyangyun.care.smart.entity.SmartDevice;
import com.zhiyangyun.care.smart.mapper.SmartAlertMapper;
import com.zhiyangyun.care.smart.mapper.SmartDeviceMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 设备健康监控：
 * <ul>
 *   <li>心跳/事件超过阈值未上报的在线设备置为离线，并生成设备离线告警，防止"哑设备"漏报；</li>
 *   <li>电量低于阈值的设备生成低电量预警。</li>
 * </ul>
 * 阈值可配置：zhiyangyun.smart.monitor.offline-minutes（默认10）、
 * zhiyangyun.smart.monitor.low-battery-percent（默认20）。
 */
@Component
public class SmartDeviceHeartbeatMonitor {

  private static final String OFFLINE_ALERT_TYPE = "DEVICE_OFFLINE";
  private static final String LOW_BATTERY_ALERT_TYPE = "DEVICE_LOW_BATTERY";
  private static final DateTimeFormatter NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  private final SmartDeviceMapper deviceMapper;
  private final SmartAlertMapper alertMapper;
  private final int offlineMinutes;
  private final int lowBatteryPercent;

  public SmartDeviceHeartbeatMonitor(
      SmartDeviceMapper deviceMapper,
      SmartAlertMapper alertMapper,
      @Value("${zhiyangyun.smart.monitor.offline-minutes:10}") int offlineMinutes,
      @Value("${zhiyangyun.smart.monitor.low-battery-percent:20}") int lowBatteryPercent) {
    this.deviceMapper = deviceMapper;
    this.alertMapper = alertMapper;
    this.offlineMinutes = offlineMinutes;
    this.lowBatteryPercent = lowBatteryPercent;
  }

  /** 每 5 分钟扫描心跳超时设备。 */
  @Scheduled(cron = "0 */5 * * * ?")
  public void scanOfflineDevices() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime cutoff = now.minusMinutes(offlineMinutes);
    List<SmartDevice> devices = deviceMapper.selectList(Wrappers.lambdaQuery(SmartDevice.class)
        .eq(SmartDevice::getIsDeleted, 0)
        .eq(SmartDevice::getEnabled, 1)
        .eq(SmartDevice::getOnlineStatus, "ONLINE"));
    for (SmartDevice device : devices) {
      LocalDateTime lastSeen = latest(device.getLastHeartbeatAt(), device.getLastEventAt());
      if (lastSeen != null && lastSeen.isAfter(cutoff)) {
        continue;
      }
      device.setOnlineStatus("OFFLINE");
      device.setUpdateTime(now);
      deviceMapper.updateById(device);
      raiseDeviceAlert(device, OFFLINE_ALERT_TYPE, "OFF", "设备离线",
          "设备[" + device.getDeviceName() + "]超过 " + offlineMinutes + " 分钟未上报心跳", now);
    }
  }

  /** 每 10 分钟扫描低电量设备（电量随心跳/事件上报刷新）。 */
  @Scheduled(cron = "0 2/10 * * * ?")
  public void scanLowBatteryDevices() {
    LocalDateTime now = LocalDateTime.now();
    List<SmartDevice> devices = deviceMapper.selectList(Wrappers.lambdaQuery(SmartDevice.class)
        .eq(SmartDevice::getIsDeleted, 0)
        .eq(SmartDevice::getEnabled, 1)
        .isNotNull(SmartDevice::getBatteryLevel)
        .le(SmartDevice::getBatteryLevel, lowBatteryPercent));
    for (SmartDevice device : devices) {
      raiseDeviceAlert(device, LOW_BATTERY_ALERT_TYPE, "BAT", "设备低电量",
          "设备[" + device.getDeviceName() + "]电量 " + device.getBatteryLevel()
              + "%，低于阈值 " + lowBatteryPercent + "%，请及时充电/更换电池", now);
    }
  }

  private void raiseDeviceAlert(SmartDevice device, String alertType, String noPrefix,
      String title, String content, LocalDateTime now) {
    Long open = alertMapper.selectCount(Wrappers.lambdaQuery(SmartAlert.class)
        .eq(SmartAlert::getIsDeleted, 0)
        .eq(SmartAlert::getDeviceId, device.getId())
        .eq(SmartAlert::getAlertType, alertType)
        .in(SmartAlert::getStatus, List.of("OPEN", "ACKNOWLEDGED")));
    if (open != null && open > 0) {
      return;
    }
    SmartAlert alert = new SmartAlert();
    alert.setOrgId(device.getOrgId());
    alert.setTenantId(device.getTenantId());
    alert.setElderId(device.getElderId());
    alert.setDeviceId(device.getId());
    alert.setAlertNo(noPrefix + now.format(NO_FMT) + device.getId());
    alert.setAlertType(alertType);
    alert.setLevel("WARNING");
    alert.setTitle(title);
    alert.setContent(content);
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

  private LocalDateTime latest(LocalDateTime a, LocalDateTime b) {
    if (a == null) {
      return b;
    }
    if (b == null) {
      return a;
    }
    return a.isAfter(b) ? a : b;
  }
}
