package com.zhiyangyun.care.govreport.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 构建上报任务请求。 */
@Data
public class GovReportBuildRequest {
  /** ORG_INFO / BED / ELDER / SERVICE / LTCI_SETTLE。 */
  @NotBlank
  private String reportType;
  /** MZ_JINMIN 民政金民 / YB_MEDICAL 医保。 */
  @NotBlank
  private String channel;
  /** 上报周期，如 2026Q2 或 202606。 */
  private String period;
  private String remark;
}
