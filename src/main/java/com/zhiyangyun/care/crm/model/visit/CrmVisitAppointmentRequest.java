package com.zhiyangyun.care.crm.model.visit;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CrmVisitAppointmentRequest {
  @NotNull(message = "请选择客户线索")
  private Long leadId;
  @NotBlank(message = "请选择预约参观时间")
  private String appointmentTime;
  @NotBlank(message = "请填写来访联系人")
  private String visitorName;
  private String visitorPhone;
  @Min(value = 1, message = "来访人数至少为1人")
  @Max(value = 20, message = "来访人数不能超过20人")
  private Integer partySize = 1;
  private String ownerStaffName;
  private String remark;
}
