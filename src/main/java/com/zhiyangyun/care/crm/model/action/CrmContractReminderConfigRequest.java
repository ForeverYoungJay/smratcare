package com.zhiyangyun.care.crm.model.action;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CrmContractReminderConfigRequest {
  @NotNull(message = "请选择是否启用提醒")
  private Boolean enabled;
  @NotNull(message = "请填写提前提醒天数")
  @Min(value = 1, message = "提前提醒天数不能小于1天")
  @Max(value = 365, message = "提前提醒天数不能超过365天")
  private Integer advanceDays;
  @NotBlank(message = "请填写发送时间")
  @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "发送时间格式应为HH:mm")
  private String sendTime;
  @NotBlank(message = "请填写短信模板")
  private String templateContent;
}
