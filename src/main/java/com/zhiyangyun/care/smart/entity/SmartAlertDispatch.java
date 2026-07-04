package com.zhiyangyun.care.smart.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("smart_alert_dispatch")
public class SmartAlertDispatch {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long alertId;
  private Long elderId;
  private Long deviceId;
  private Long ruleId;
  private String level;
  private String dispatchStatus;
  private Long assigneeId;
  private String assigneeName;
  private LocalDateTime triggeredAt;
  private LocalDateTime assignedAt;
  private LocalDateTime respondedAt;
  private LocalDateTime onsiteAt;
  private LocalDateTime handledAt;
  private LocalDateTime reviewedAt;
  private LocalDateTime responseDeadline;
  private Integer escalationCount;
  private String handleNote;
  private String reviewNote;
  private Long incidentId;
  private Long createdBy;
  @TableField("create_time")
  private LocalDateTime createTime;
  @TableField("update_time")
  private LocalDateTime updateTime;
  @TableField("is_deleted")
  private Integer isDeleted;
}
