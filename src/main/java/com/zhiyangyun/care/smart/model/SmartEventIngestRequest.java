package com.zhiyangyun.care.smart.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 厂商网关标准事件上报（统一事件模型）：设备侧只需上报 device_sn / event_type / payload / occurred_at，
 * 具体厂商协议差异在网关适配层消化。
 */
@Data
public class SmartEventIngestRequest {
  /** 设备序列号（对应设备台账 device_code，需全局唯一）。 */
  @NotBlank
  private String deviceSn;
  /** 标准事件类型：HEARTBEAT / FALL / SOS / BED_EXIT_TIMEOUT / GEO_FENCE / LINGER / VITAL（兼容旧别名）。 */
  @NotBlank
  private String eventType;
  /** 事件发生时间，缺省取服务端当前时间。 */
  private LocalDateTime occurredAt;
  /** 事件载荷（指标键值，如 heartRate/spo2/batteryLevel/signalStrength）。 */
  private JsonNode payload;
  /** 位置描述（可空，缺省取设备台账位置）。 */
  private String location;
  /** 联动影像引用（视频厂商适配层预留）。 */
  private String mediaRef;
  /** 联动定位引用（定位厂商适配层预留）。 */
  private String locationRef;
  /** 电量百分比 0-100（也可放 payload.batteryLevel）。 */
  private Integer batteryLevel;
  /** 信号强度 0-100（也可放 payload.signalStrength）。 */
  private Integer signalStrength;
  /** 固件版本。 */
  private String firmwareVersion;
}
