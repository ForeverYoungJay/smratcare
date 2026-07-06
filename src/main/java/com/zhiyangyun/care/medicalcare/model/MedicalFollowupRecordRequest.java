package com.zhiyangyun.care.medicalcare.model;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

/** 新增随访记录请求（下次随访日期按计划频次自动排程）。 */
@Data
public class MedicalFollowupRecordRequest {
  @NotNull
  private Long planId;
  private LocalDate followupDate;
  /** 本次体征值 JSON。 */
  private String vitalJson;
  /** 关联 health_data_record.id。 */
  private Long healthDataId;
  /** GOOD / PARTIAL / POOR。 */
  private String medicationCompliance;
  /** CONTROLLED / UNSTABLE / WORSENED。 */
  private String assessmentLevel;
  private String assessment;
  /** 不传则按计划频次自动计算。 */
  private LocalDate nextFollowupDate;
  private String remark;
}
