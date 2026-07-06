package com.zhiyangyun.care.medicalcare.model;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

/** 创建/更新巡诊排班请求。 */
@Data
public class MedicalRoundsPlanRequest {
  @NotNull
  private LocalDate planDate;
  private String timeSlot;
  private Long doctorId;
  private String doctorName;
  private String area;
  /** ALL / AREA / CUSTOM。 */
  private String elderScope;
  private String elderIdsJson;
  private String remark;
}
