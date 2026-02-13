package com.zhiyangyun.care.vital.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class VitalRecordResponse {
  private Long id;
  private Long elderId;
  private String elderName;
  private String type;
  private String valueJson;
  private LocalDateTime measuredAt;
  private Long recordedByStaffId;
  private String recordedByStaffName;
  private boolean abnormalFlag;
  private String abnormalReason;
  private String remark;
}
