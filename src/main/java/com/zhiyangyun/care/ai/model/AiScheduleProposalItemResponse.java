package com.zhiyangyun.care.ai.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AiScheduleProposalItemResponse {
  private Long id;
  private Long proposalId;
  private Long staffId;
  private String staffName;
  private LocalDate dutyDate;
  private String shiftCode;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private Integer nightShift;
  private Integer manualAdjusted;
  private String violationNote;
  private Long scheduleId;
}
