package com.zhiyangyun.care.crm.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("crm_stage_transition_log")
public class CrmStageTransitionLog {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private String entityType;
  private Long leadId;
  private Long contractId;
  private String transitionType;
  private String source;
  private String fromStage;
  private String toStage;
  private String fromStatus;
  private String toStatus;
  private String fromOwnerDept;
  private String toOwnerDept;
  private String remark;
  private Long operatedBy;
  private String operatedByName;
  private LocalDateTime operatedAt;
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
