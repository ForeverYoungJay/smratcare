package com.zhiyangyun.care.emr.model;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

/** 电子病历新增/编辑请求。 */
@Data
public class EmrRecordRequest {
  private Long id;
  @NotNull
  private Long elderId;
  /** ADMISSION / PROGRESS / DIAGNOSIS / EXAM。 */
  private String recordType;
  private LocalDate visitDate;
  private String chiefComplaint;
  private String presentIllness;
  private String pastHistory;
  private String physicalExam;
  private String diagnosis;
  private String treatmentPlan;
  private String remark;
}
