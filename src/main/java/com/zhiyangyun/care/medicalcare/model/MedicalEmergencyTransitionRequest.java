package com.zhiyangyun.care.medicalcare.model;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.Data;

/** 急救事件状态流转请求。 */
@Data
public class MedicalEmergencyTransitionRequest {
  /** CALL 登记120呼叫 / DEPART 送医 / OUTCOME 转归 / CLOSE 关闭 / CANCEL 取消。 */
  @NotBlank
  private String action;
  /** CALL：呼叫时间，默认当前时间。 */
  private LocalDateTime callTime;
  /** DEPART：送医医院。 */
  private String hospitalName;
  /** DEPART：陪同人。 */
  private String escortName;
  /** DEPART：出发时间，默认当前时间。 */
  private LocalDateTime departTime;
  /** OUTCOME：RETURNED / HOSPITALIZED / DECEASED。 */
  private String outcome;
  private String outcomeNote;
  private String remark;
}
