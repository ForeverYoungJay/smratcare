package com.zhiyangyun.care.ltci.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 受理评估申请并分配评估员。 */
@Data
public class LtciAcceptRequest {
  @NotNull
  private Long applyId;
  private Long assessorId;
  private String remark;
}
