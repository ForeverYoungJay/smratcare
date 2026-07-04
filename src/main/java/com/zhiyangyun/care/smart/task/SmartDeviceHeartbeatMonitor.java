package com.zhiyangyun.care.smart.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.smart.entity.SmartAlert;
import com.zhiyangyun.care.smart.entity.SmartDevice;
import com.zhiyangyun.care.smart.mapper.SmartAlertMapper;
import com.zhiyangyun.care.smart.mapper.SmartDeviceMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 设备心跳离线监控：心跳/事件超过阈值未上报的在线设备置为离线，并生成设备离线告警，
 * 防止"哑设备"漏报。
 */
@Component
public class SmartDeviceHeartbeatMonitor {

  private static final int OFFLINE_THRESHOLD_MINUTES = 10;
  private static final String OFFLINE_ALERT_TYPE = "DEVICE_OFFLINE";
  private static final DateTimeFormatter NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  private final SmartDeviceMapper deviceMapper;
  private final SmartAlertMapper alertMapper;

  public SmartDeviceHeartbeatMonitor(SmartDeviceMapper deviceMapper, SmartAlertMapper alertMapper) {
    this.deviceMapper = deviceMapper;
    this.alertMapper = alertMapper;
  }

  @Scheduled(cron = "0 */5 * * * ?")
  public void scanOfflineDevices() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime cutoff = now.minusMinutes(OFFLINE_THRESHOLD_MINUTES);
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
      raiseOfflineAlert(device, now);
    }
  }

  private void raiseOfflineAlert(SmartDevice device, LocalDateTime now) {
    Long open = alertMapper.selectCount(Wrappers.lambdaQuery(SmartAlert.class)
        .eq(SmartAlert::getIsDeleted, 0)
        .eq(SmartAlert::getDeviceId, device.getId())
        .eq(SmartAlert::getAlertType, OFFLINE_ALERT_TYPE)
        .in(SmartAlert::getStatus, List.of("OPEN", "ACKNOWLEDGED")));
    if (open != null && open > 0) {
      return;
    }
    SmartAlert alert = new SmartAlert();
    alert.setOrgId(device.getOrgId());
    alert.setTenantId(device.getTenantId());
    alert.setElderId(device.getElderId());
    alert.setDeviceId(device.getId());
    alert.setAlertNo("OFF" + now.format(NO_FMT) + device.getId());
    alert.setAlertType(OFFLINE_ALERT_TYPE);
    alert.setLevel("WARNING");
    alert.setTitle("设备离线");
    alert.setContent("设备[" + device.getDeviceName() + "]超过 " + OFFLINE_THRESHOLD_MINUTES + " 分钟未上报心跳");
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
