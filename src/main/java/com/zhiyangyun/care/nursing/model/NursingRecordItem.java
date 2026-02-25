package com.zhiyangyun.care.nursing.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class NursingRecordItem {
  private String recordType;
  private Long sourceId;
  private LocalDateTime recordTime;
  private Long elderId;
  private String elderName;
  private Long staffId;
  private String staffName;
  private String serviceName;
  private Long planId;
  private String planName;
  private String status;
  private Integer successFlag;
  private String remark;
}
