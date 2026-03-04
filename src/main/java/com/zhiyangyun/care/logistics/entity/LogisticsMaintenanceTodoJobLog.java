package com.zhiyangyun.care.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("logistics_maintenance_todo_job_log")
public class LogisticsMaintenanceTodoJobLog {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long tenantId;

  private Long orgId;

  private String triggerType;

  private String status;

  private Integer days;

  private Long totalMatched;

  private Long createdCount;

  private Long skippedCount;

  private String errorMessage;

  private LocalDateTime executedAt;

  private Long createdBy;

  @TableField("create_time")
  private LocalDateTime createTime;

  @TableField("update_time")
  private LocalDateTime updateTime;

  @TableField("is_deleted")
  private Integer isDeleted;
}
