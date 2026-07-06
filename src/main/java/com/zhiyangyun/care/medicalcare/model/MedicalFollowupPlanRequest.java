package com.zhiyangyun.care.medicalcare.model;

import java.time.LocalDate;
import lombok.Data;

/** 创建/更新慢病随访计划请求。 */
@Data
public class MedicalFollowupPlanRequest {
  private Long elderId;
  private String elderName;
  /** HYPERTENSION / DIABETES / COPD / CHD / STROKE / OTHER。 */
  private String diseaseType;
  private String planName;
  /** 随访间隔天数，默认 30。 */
  private Integer frequencyDays;
  private String targetIndicators;
  private LocalDate nextFollowupDate;
  private Long doctorId;
  private String doctorName;
  private String remark;
}
