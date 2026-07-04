package com.zhiyangyun.care.ltci.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 提交失能评估申请。 */
@Data
public class LtciApplyRequest {
  @NotNull
  private Long elderId;
  /** FIRST 初评 / REVIEW 复评 / DISPUTE 争议复核，默认 FIRST。 */
  private String applyType;
  private String applySource;
  private String applicantName;
  private String applicantPhone;
  private String remark;
}
