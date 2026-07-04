package com.zhiyangyun.care.medorder.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 医嘱执行签核请求。 */
@Data
public class MedicalOrderExecRequest {
  @NotNull
  private Long executionId;
  /** DONE 已执行 / SKIPPED 跳过。 */
  @NotNull
  private String status;
  private String result;
  private Long dispenseRecordId;
  private String remark;
}
