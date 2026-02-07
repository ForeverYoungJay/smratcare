package com.zhiyangyun.care.vital.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class VitalRecordResponse {
  private Long id;
  private Long elderId;
  private String type;
  private String valueJson;
  private LocalDateTime measuredAt;
  private Long recordedByStaffId;
  private boolean abnormalFlag;
  private String abnormalReason;
  private String remark;
}
