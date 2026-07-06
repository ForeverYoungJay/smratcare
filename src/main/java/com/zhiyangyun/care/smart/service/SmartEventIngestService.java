package com.zhiyangyun.care.smart.service;

import com.zhiyangyun.care.smart.model.SmartEventIngestRequest;
import com.zhiyangyun.care.smart.model.SmartEventIngestResponse;

/**
 * 厂商网关标准事件接入：统一事件模型 → 设备心跳/电量刷新 → 场景规则引擎匹配 → 告警 → 自动派单，
 * 打通 IoT 安全预警闭环的入口。
 */
public interface SmartEventIngestService {

  /** 处理一条标准设备事件（心跳事件仅刷新设备健康状态与低电量预警，不走场景规则）。 */
  SmartEventIngestResponse ingest(SmartEventIngestRequest request);
}
