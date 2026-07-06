package com.zhiyangyun.care.medicalcare.model;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;

/** 保存抢救记录单请求（每个事件一份，重复提交覆盖更新）。 */
@Data
public class MedicalRescueRecordRequest {
  @NotNull
  private Long eventId;
  /** 时间线明细 JSON。 */
  private String timelineJson;
  private String participants;
  private String drugsUsed;
  private String measures;
  /** SUCCESS / TRANSFERRED / FAILED。 */
  private String result;
  private String resultNote;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private String remark;
}
