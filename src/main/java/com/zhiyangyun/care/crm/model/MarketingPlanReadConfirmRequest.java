package com.zhiyangyun.care.crm.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MarketingPlanReadConfirmRequest {
  @NotBlank(message = "确认动作不能为空")
  @Pattern(regexp = "AGREE|IMPROVE", message = "确认动作仅支持 AGREE 或 IMPROVE")
  private String action;

  @Size(max = 1000, message = "改进详情长度不能超过 1000")
  private String actionDetail;
}
