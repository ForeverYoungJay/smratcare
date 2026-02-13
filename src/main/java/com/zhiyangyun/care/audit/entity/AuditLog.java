package com.zhiyangyun.care.audit.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("audit_log")
public class AuditLog {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long actorId;
  private String actorName;
  private String actionType;
  private String entityType;
  private Long entityId;
  private String detail;
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;
}
