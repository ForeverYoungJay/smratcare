package com.zhiyangyun.care.crm.model.visit;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CrmVisitAppointmentCompleteRequest {
  @NotBlank(message = "请填写参观结果")
  private String visitResult;
  private String nextFollowDate;
  private String remark;
}
