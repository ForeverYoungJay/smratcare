package com.zhiyangyun.care.ai.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("ai_schedule_proposal_item")
public class AiScheduleProposalItem {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
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
  /** 采纳后写回 staff_schedule 的记录 id */
  private Long scheduleId;
  private Long createdBy;
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
