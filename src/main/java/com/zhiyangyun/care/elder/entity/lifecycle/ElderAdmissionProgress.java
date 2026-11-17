package com.zhiyangyun.care.elder.entity.lifecycle;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 入住办理进度：每位长者一条，保存暂存草稿、当前步骤与退回补充原因。 */
@Data
@TableName("elder_admission_progress")
public class ElderAdmissionProgress {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long elderId;
  // 允许被清空（恢复办理时清掉退回原因等）
  @TableField(updateStrategy = FieldStrategy.ALWAYS)
  private String contractNo;
  private Integer currentStep;
  private String status;
  @TableField(updateStrategy = FieldStrategy.ALWAYS)
  private String returnReason;
  @TableField(updateStrategy = FieldStrategy.ALWAYS)
  private String draftJson;
  private Long updatedBy;
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
