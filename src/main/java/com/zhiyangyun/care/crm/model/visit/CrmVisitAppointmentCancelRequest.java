package com.zhiyangyun.care.crm.model.visit;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CrmVisitAppointmentCancelRequest {
  @NotBlank(message = "请填写取消原因")
  private String cancelReason;
}
