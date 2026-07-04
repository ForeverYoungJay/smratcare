package com.zhiyangyun.care.elder.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ElderCareLevelAdjustmentRequest {
  @NotBlank(message = "护理等级不能为空")
  private String careLevel;

  private String reason;

  private Long assessmentRecordId;
}
