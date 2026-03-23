package com.zhiyangyun.care.schedule.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ShiftSwapRequestResponse {
  private Long id;
  private Long applicantStaffId;
  private String applicantStaffName;
  private Long applicantScheduleId;
  private LocalDate applicantDutyDate;
  private String applicantShiftCode;
  private LocalDateTime applicantStartTime;
  private LocalDateTime applicantEndTime;
  private Long targetStaffId;
  private String targetStaffName;
  private Long targetScheduleId;
  private LocalDate targetDutyDate;
  private String targetShiftCode;
  private LocalDateTime targetStartTime;
  private LocalDateTime targetEndTime;
  private String status;
  private String targetConfirmStatus;
  private Long approvalId;
  private String approvalStatus;
  private String applicantRemark;
  private String targetRemark;
  private LocalDateTime targetConfirmedAt;
  private LocalDateTime approvalSubmittedAt;
  private LocalDateTime completedAt;
  private LocalDateTime createTime;
}
