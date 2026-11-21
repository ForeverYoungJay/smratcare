package com.zhiyangyun.care.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExecuteTaskRequest {
  @NotNull
  private Long taskDailyId;

  /** 服务端以登录人覆盖，客户端无需传 */
  private Long staffId;

  @NotBlank
  private String bedQrCode;

  private String remark;
}
