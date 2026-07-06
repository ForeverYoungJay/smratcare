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
@TableName("ai_schedule_proposal")
public class AiScheduleProposal {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private String title;
  private Long deptId;
  private String deptName;
  private LocalDate dateFrom;
  private LocalDate dateTo;
  private String shiftCodes;
  private String staffIds;
  /** DRAFT/GENERATED/ADOPTED */
  private String status;
  private String constraintSnapshotJson;
  private String scoreJson;
  private Integer unfilledCount;
  private LocalDateTime generatedAt;
  private LocalDateTime adoptedAt;
  private Long adoptedBy;
  private String remark;
  private Long createdBy;
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
