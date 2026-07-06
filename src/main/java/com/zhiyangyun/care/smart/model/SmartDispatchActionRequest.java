package com.zhiyangyun.care.smart.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 派单操作请求（受理/响应/到场/处置/复盘）。 */
@Data
public class SmartDispatchActionRequest {
  @NotNull
  private Long dispatchId;
  private Long assigneeId;
  private String assigneeName;
  private String note;
  private Long incidentId;
  /** 处置时一键生成不良事件记录（与 incidentId 二选一）。 */
  private Boolean createIncident;
  /** 一键生成不良事件时的事故类型（缺省取告警类型）。 */
  private String incidentType;
}
