package com.zhiyangyun.care.schedule.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("shift_swap_request")
public class ShiftSwapRequest {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
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
  private Long createdBy;
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
