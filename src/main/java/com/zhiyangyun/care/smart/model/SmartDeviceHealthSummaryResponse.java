package com.zhiyangyun.care.smart.model;

import lombok.Data;

/** 设备健康监控汇总。 */
@Data
public class SmartDeviceHealthSummaryResponse {
  private Long totalCount = 0L;
  private Long onlineCount = 0L;
  private Long offlineCount = 0L;
  private Long lowBatteryCount = 0L;
  private Long weakSignalCount = 0L;
  /** 心跳超时但仍标记在线（待离线扫描处理）的设备数。 */
  private Long staleHeartbeatCount = 0L;
  private Integer lowBatteryThreshold;
  private Integer weakSignalThreshold;
  private Integer offlineMinutes;
}
