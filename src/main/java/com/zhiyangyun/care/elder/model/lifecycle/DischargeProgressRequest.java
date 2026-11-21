package com.zhiyangyun.care.elder.model.lifecycle;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DischargeProgressRequest {
  /** CARE_HANDOVER / ITEM_HANDOVER / ARCHIVE */
  @NotBlank
  private String step;
}
