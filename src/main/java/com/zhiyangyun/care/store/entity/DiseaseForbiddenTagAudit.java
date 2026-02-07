package com.zhiyangyun.care.store.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("disease_forbidden_tag_audit")
public class DiseaseForbiddenTagAudit {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long orgId;
  private Long diseaseId;
  private String actionType;
  private String beforeTagIds;
  private String afterTagIds;
  private Long operatorStaffId;
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
