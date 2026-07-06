package com.zhiyangyun.care.medicalcare.model;

import java.time.LocalDateTime;
import lombok.Data;

/** 一键发起急救事件请求。 */
@Data
public class MedicalEmergencyEventRequest {
  private Long elderId;
  private String elderName;
  /** 预留 IoT SOS 告警关联。 */
  private Long alertId;
  private LocalDateTime eventTime;
  private String location;
  private String symptom;
  private String remark;
}
