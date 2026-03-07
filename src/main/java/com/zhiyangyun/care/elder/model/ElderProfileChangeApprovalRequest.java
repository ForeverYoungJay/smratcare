package com.zhiyangyun.care.elder.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ElderProfileChangeApprovalRequest {
  @NotBlank(message = "申请原因不能为空")
  private String reason;

  private String module;

  private String changeSummary;
}
