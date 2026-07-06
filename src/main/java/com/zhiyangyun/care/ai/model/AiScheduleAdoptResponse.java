package com.zhiyangyun.care.ai.model;

import lombok.Data;

/** 智能排班：采纳方案（写回 staff_schedule）结果。 */
@Data
public class AiScheduleAdoptResponse {
  private Long proposalId;
  private int createdCount;
  private int skippedCount;
}
